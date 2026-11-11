from pathlib import Path
import re
import json
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.opc.constants import RELATIONSHIP_TYPE as RT
from PIL import Image

OUT = Path(__file__).resolve().parent
EVIDENCE = json.loads((OUT / 'evidence.json').read_text(encoding='utf-8'))['sources']

def clean(s):
    s = re.sub(r'\[([^]]+)\]\(([^)]+)\)', r'\1', s)
    return s.replace('`', '').replace('**', '')

def add_content(p, text):
    cursor = 0
    for m in re.finditer(r'\[([^]]+)\]\(([^)]+)\)', text):
        p.add_run(clean(text[cursor:m.start()]))
        label, url = m.groups()
        if url.startswith('https://'):
            hyperlink = OxmlElement('w:hyperlink')
            hyperlink.set(qn('r:id'), p.part.relate_to(url, RT.HYPERLINK, is_external=True))
            run = OxmlElement('w:r')
            prop = OxmlElement('w:rPr')
            style = OxmlElement('w:rStyle')
            style.set(qn('w:val'), 'Hyperlink')
            prop.append(style)
            run.append(prop)
            value = OxmlElement('w:t')
            value.text = label
            run.append(value)
            hyperlink.append(run)
            p._p.append(hyperlink)
        else:
            p.add_run(label)
        cursor = m.end()
    p.add_run(clean(text[cursor:]))

def prevent_row_split(row):
    prop = row._tr.get_or_add_trPr()
    prop.append(OxmlElement('w:cantSplit'))

def add_table(doc, lines):
    rows = [[clean(x.strip()) for x in line.strip().strip('|').split('|')] for line in lines]
    rows = [row for row in rows if not all(re.fullmatch(r'[:\- ]+', x) for x in row)]
    table = doc.add_table(rows=1, cols=len(rows[0]))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    n = len(rows[0])
    if n == 5:
        widths = [1.42, 1.02, 2.0, .55, .51] if rows[0][0] == 'Column' else [.38, 2.6, .7, .75, 1.07]
    elif n == 4:
        widths = [.55, 2.0, 1.6, 1.35]
    elif n == 3:
        widths = [1.48, 1.7, 2.82]
    else:
        widths = [1.6, 4.4]
    widths = [x * 6.9 / sum(widths) for x in widths]
    for col, width in zip(table.columns, widths):
        col.width = Inches(width)
    for ridx, row_text in enumerate(rows):
        row = table.rows[0] if ridx == 0 else table.add_row()
        prevent_row_split(row)
        if ridx == 0:
            row._tr.get_or_add_trPr().append(OxmlElement('w:tblHeader'))
        for i, (cell, value) in enumerate(zip(row.cells, row_text)):
            cell.width = Inches(widths[i])
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            tcpr = cell._tc.get_or_add_tcPr()
            shade = OxmlElement('w:shd')
            shade.set(qn('w:fill'), '183B56' if ridx == 0 else ('F1F5F9' if ridx % 2 == 0 else 'FFFFFF'))
            tcpr.append(shade)
            mar = OxmlElement('w:tcMar')
            for edge in ['top', 'bottom', 'left', 'right']:
                e = OxmlElement('w:' + edge)
                e.set(qn('w:w'), '90')
                e.set(qn('w:type'), 'dxa')
                mar.append(e)
            tcpr.append(mar)
            borders = OxmlElement('w:tcBorders')
            for edge in ['top', 'bottom', 'left', 'right']:
                e = OxmlElement('w:' + edge)
                for k, v in [('val', 'single'), ('sz', '4'), ('color', 'D9D9D9')]:
                    e.set(qn('w:' + k), v)
                borders.append(e)
            tcpr.append(borders)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.line_spacing = 1.05
            r = p.add_run(value)
            r.font.size = Pt(9 if n >= 5 else 9.5)
            if ridx == 0:
                r.bold = True
                r.font.color.rgb = RGBColor.from_string('FFFFFF')
    for cell in table.rows[0].cells:
        for p in cell.paragraphs:
            p.paragraph_format.keep_with_next = True
    if len(rows) <= 8:
        for row in table.rows[:-1]:
            for cell in row.cells:
                for p in cell.paragraphs:
                    p.paragraph_format.keep_with_next = True
    doc.add_paragraph().paragraph_format.space_after = Pt(1)

for stem in ['design-document', 'er-diagram', 'threat-model']:
    md = (OUT / (stem + '.md')).read_text(encoding='utf-8')
    doc = Document()
    section = doc.sections[0]
    section.page_width, section.page_height = Inches(8.5), Inches(11)
    section.top_margin, section.bottom_margin = Inches(.65), Inches(.65)
    section.left_margin, section.right_margin = Inches(.8), Inches(.8)
    normal = doc.styles['Normal']
    normal.font.name = 'Calibri'
    normal.font.size = Pt(11)
    normal.paragraph_format.line_spacing = 1.12
    normal.paragraph_format.space_after = Pt(7)
    for name, size, color in [('Title', 25, '000000'), ('Heading 1', 16, '183B56'), ('Heading 2', 12, '183B56')]:
        style = doc.styles[name]
        style.font.name = 'Calibri'
        style.font.size = Pt(size)
        style.font.color.rgb = RGBColor.from_string(color)
        style.paragraph_format.space_before = Pt(14 if name != 'Title' else 0)
        style.paragraph_format.space_after = Pt(6)
    doc.styles['Title'].font.bold = True
    footer = section.footer.paragraphs[0]
    footer.alignment = 2
    run = footer.add_run('Choreemon  |  ')
    run.font.size = Pt(9)
    fld = OxmlElement('w:fldSimple')
    fld.set(qn('w:instr'), 'PAGE')
    footer._p.append(fld)
    lines = md.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        if not line.strip():
            i += 1
            continue
        if line.startswith('|'):
            block = []
            while i < len(lines) and lines[i].startswith('|'):
                block.append(lines[i]); i += 1
            add_table(doc, block)
            continue
        image = re.fullmatch(r'!\[([^]]*)\]\(([^)]+)\)', line)
        if image:
            filename = OUT / image.group(2)
            w, h = Image.open(filename).size
            width = min(6.9, 5.6 * w / h)
            p = doc.add_paragraph()
            p.alignment = 1
            p.paragraph_format.keep_with_next = True
            run = p.add_run()
            run.add_picture(str(filename), width=Inches(width))
            p2 = doc.add_paragraph(image.group(1))
            p2.alignment = 1
            p2.runs[0].italic = True
            p2.runs[0].font.size = Pt(9)
        elif line.startswith('# '):
            doc.add_paragraph(clean(line[2:]), 'Title')
        elif line.startswith('## '):
            doc.add_paragraph(clean(line[3:]), 'Heading 1')
        elif line.startswith('### '):
            doc.add_paragraph(clean(line[4:]), 'Heading 2')
        elif re.match(r'^\d+\. ', line):
            doc.add_paragraph(clean(re.sub(r'^\d+\. ', '', line)), 'List Number')
        else:
            add_content(doc.add_paragraph(), line)
        i += 1
    doc.add_heading('Source evidence index', 1)
    doc.add_paragraph('Evidence IDs identify the following workspace files and line locations. Full SHA256 hashes and exact symbols are retained in the accompanying evidence.json file. Paths are relative to ProjectCII.')
    doc.add_paragraph('Compact path notation: [app] expands to app/src/main/java/com/choreemon/app and [backend] expands to src/main/kotlin/com/choreemon/backend. Both retain their project directory prefix.')
    used = set(re.findall(r'\b[ABV]\d+\b', md))
    # Include all source IDs in a named range such as B14 to B18.
    for group, start, end in re.findall(r'\b([ABV])(\d+) to [ABV](\d+)\b', md):
        used.update(f'{group}{v}' for v in range(int(start), int(end)+1))
    for entry in EVIDENCE:
        if entry['id'] not in used:
            continue
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(4)
        run = p.add_run(entry['id'] + '  ')
        run.bold = True
        # Soft wrapping opportunities after path separators preserve readable source locations.
        short = entry['path'].replace('app/src/main/java/com/choreemon/app/', '[app]/').replace('src/main/kotlin/com/choreemon/backend/', '[backend]/')
        locations = sorted({n for loc in entry['locators'] for n in loc['lines']})
        p.add_run(short.replace('/', '/\u200b') + '  lines ' + ', '.join(map(str, locations)))
        for run in p.runs:
            run.font.size = Pt(9)
    for element in [doc.element, doc.styles.element]:
        for border in element.xpath('.//w:pBdr'):
            border.getparent().remove(border)
    doc.core_properties.title = clean(md.splitlines()[0][2:])
    doc.core_properties.subject = 'Source analysis of Choreemon Android and Ktor backend'
    doc.core_properties.author = 'Codebase analysis'
    doc.save(OUT / (stem + '.docx'))
    print(stem + '.docx created')
