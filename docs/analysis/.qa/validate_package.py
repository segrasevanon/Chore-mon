from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import json
import hashlib
import re
from docx import Document
from lxml import etree

base = Path(__file__).resolve().parent.parent
root = base.parent.parent
data = json.loads((base / 'evidence.json').read_text(encoding='utf-8'))
for source in data['sources']:
    assert hashlib.sha256((root / source['path']).read_bytes()).hexdigest() == source['sha256'], source['path']
schema = json.loads((base / 'schema.json').read_text())
assert len(schema) == 8
assert sum(len(t['fk']) for t in schema) == 10
assert sum(c['nullable'] for t in schema for c in t['columns']) == 2
assert [t['name'] for t in schema if len(t['pk']) > 1] == ['ChoreAssignments', 'ClaimedMilestones']
for md in base.glob('*.md'):
    text = md.read_text(encoding='utf-8')
    for target in re.findall(r'\]\(([^)]+)\)', text):
        if target.startswith('https://'):
            continue
        file = target.split('#')[0]
        assert (base / file).exists(), (md.name, target)
for stem in ['design-document','er-diagram','threat-model']:
    with ZipFile(base / (stem + '.docx')) as docx:
        for name in ['word/document.xml', 'word/styles.xml']:
            doc = etree.fromstring(docx.read(name))
            assert not doc.xpath('//*[local-name()="pBdr"]')
    doc = Document(base / (stem + '.docx'))
    assert len(doc.inline_shapes) == 1
    assert len(doc.tables) > 0
    assert all(p.text.strip() for p in doc.paragraphs if p.style.name.startswith('Heading'))
report = {
    'review_date': '2026-10-19',
    'source_hashes_verified': len(data['sources']),
    'tables': 8,
    'foreign_keys': 10,
    'columns': sum(len(t['columns']) for t in schema),
    'threats': len(re.findall(r'^### T\d+', (base / 'threat-model.md').read_text(), re.M)),
    'rendering': 'Packaged renderer unavailable because LibreOffice is absent; rendered with installed Microsoft Word and bundled Poppler.',
    'application_tests': 'Not rerun; documentation-only analysis.',
    'application_source_changes': 'None; reviewed source hashes unchanged.'
}
(base / '.qa/package-validation.json').write_text(json.dumps(report, indent=2))
package = base / 'choreemon-codebase-documentation.zip'
with ZipFile(package, 'w', ZIP_DEFLATED) as archive:
    for file in sorted(base.rglob('*')):
        if not file.is_file() or '.qa' in file.relative_to(base).parts or file == package:
            continue
        archive.write(file, file.relative_to(base).as_posix())
print(json.dumps(report, indent=2))
print('Package bytes:', package.stat().st_size)
