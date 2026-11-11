from pathlib import Path
from pdf2image import convert_from_path, pdfinfo_from_path
from PIL import Image, ImageOps, ImageDraw
import json
from docx import Document

base = Path(__file__).resolve().parent
poppler = r'C:\Users\rites\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\poppler\Library\bin'
counts = {}
for stem in ['design-document', 'er-diagram', 'threat-model']:
    folder = base / stem
    pdf = folder / (stem + '.pdf')
    info = pdfinfo_from_path(str(pdf), poppler_path=poppler)
    pages = convert_from_path(str(pdf), dpi=110, poppler_path=poppler)
    counts[stem] = {'pages': len(pages), 'bytes': (base.parent / (stem + '.docx')).stat().st_size}
    thumbs = []
    for i, page in enumerate(pages, 1):
        page.save(folder / f'page-{i}.png')
        thumb = page.copy()
        thumb.thumbnail((350, 460))
        card = Image.new('RGB', (370, 490), '#dbe2e8')
        card.paste(thumb, ((370-thumb.width)//2, 5))
        ImageDraw.Draw(card).text((12, 470), f'{stem} page {i}', fill='black')
        thumbs.append(card)
    for batch in range(0, len(thumbs), 8):
        group = thumbs[batch:batch+8]
        sheet = Image.new('RGB', (370*4, 490*((len(group)+3)//4)), 'white')
        for i, thumb in enumerate(group):
            sheet.paste(thumb, ((i%4)*370, (i//4)*490))
        sheet.save(base / f'{stem}-contact-{batch//8+1}.png')
    document = Document(base.parent / (stem + '.docx'))
    assert document.paragraphs[0].style.name == 'Title'
    for table in document.tables:
        assert len(table.rows) > 1
        assert all(len(row.cells) == len(table.columns) for row in table.rows)
    assert not any('TODO' in p.text or 'turn0view' in p.text for p in document.paragraphs)
(base / 'validation.json').write_text(json.dumps(counts, indent=2))
print(json.dumps(counts, indent=2))
