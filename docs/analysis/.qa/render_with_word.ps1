$ErrorActionPreference = 'Stop'
$outputRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    foreach ($stem in @('design-document','er-diagram','threat-model')) {
        $source = Join-Path $outputRoot "$stem.docx"
        $qaDir = Join-Path $PSScriptRoot $stem
        New-Item -ItemType Directory -Path $qaDir -Force | Out-Null
        $pdf = Join-Path $qaDir "$stem.pdf"
        $document = $word.Documents.Open($source, $false, $true, $false)
        try {
            $document.Repaginate()
            $document.ExportAsFixedFormat($pdf, 17)
            Write-Output "$stem exported"
        } finally {
            $document.Close(0)
            [System.Runtime.InteropServices.Marshal]::ReleaseComObject($document) | Out-Null
        }
    }
} finally {
    $word.Quit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
}
