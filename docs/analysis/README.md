# Choreemon codebase documentation

Generated from source reviewed on 19 October 2026. The Android app runs offline; the independent backend is not integrated with it.

- [Software design document](design-document.md) and [Word copy](design-document.docx)
- [Entity relationship document and data dictionary](er-diagram.md) and [Word copy](er-diagram.docx)
- [Full ER diagram](diagrams/er-full.svg), [key relationship overview](diagrams/er-overview.svg) and [editable Mermaid source](diagrams/er-diagram.mmd)
- [Threat model](threat-model.md) and [Word copy](threat-model.docx)
- [Source evidence index](source-index.md), [source hashes](evidence.json) and [extracted schema](schema.json)

The design distinguishes implemented components, placeholders and recommendations. The threat model contains 14 prioritized findings and validation cases. No application source was changed and no live deployment was exercised. Existing Android test results are historical and were not rerun.

To update: run build_documents.py with the bundled Python runtime, render_diagrams.cjs with the bundled Node runtime, then export_docx.py with the bundled Python runtime. render_diagrams.cjs expects NODE_PATH to point to the bundled node_modules. Word copies must be rendered and visually checked after regeneration. QA scripts and images are in the local .qa directory and are not deliverables.
