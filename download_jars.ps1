$ErrorActionPreference = 'Stop'
$libPath = Join-Path $PSScriptRoot "lib"

# Define destination subfolders
$javafxDir = Join-Path $libPath "javafx"
$mysqlDir = Join-Path $libPath "mysqlconnector"
$atlantafxDir = Join-Path $libPath "atlantafx"
$gsonDir = Join-Path $libPath "gson"
$zxingDir = Join-Path $libPath "zxing"

# Create directories if they don't exist
foreach ($dir in @($javafxDir, $mysqlDir, $atlantafxDir, $gsonDir, $zxingDir)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
}

# Download individual jars
$jarUrls = @{
    "atlantafx-base-2.0.1.jar" = "https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.0.1/atlantafx-base-2.0.1.jar"
    "mysql-connector-j-8.4.0.jar" = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
    "core-3.5.3.jar" = "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.3/core-3.5.3.jar"
    "javase-3.5.3.jar" = "https://repo1.maven.org/maven2/com/google/zxing/javase/3.5.3/javase-3.5.3.jar"
    "gson-2.11.0.jar" = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
}

# Map jar files to their specific subdirectories
$jarMappings = @{
    "atlantafx-base-2.0.1.jar" = $atlantafxDir
    "mysql-connector-j-8.4.0.jar" = $mysqlDir
    "core-3.5.3.jar" = $zxingDir
    "javase-3.5.3.jar" = $zxingDir
    "gson-2.11.0.jar" = $gsonDir
}

foreach ($fileName in $jarUrls.Keys) {
    $url = $jarUrls[$fileName]
    $destDir = $jarMappings[$fileName]
    $destPath = Join-Path $destDir $fileName
    Write-Host "Downloading $fileName to $destDir..."
    Invoke-WebRequest -Uri $url -OutFile $destPath
}

# Download JavaFX SDK (22.0.1) for Windows x64
$jfxUrl = "https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_windows-x64_bin-sdk.zip"
$jfxZip = "javafx-sdk.zip"
Write-Host "Downloading JavaFX SDK..."
Invoke-WebRequest -Uri $jfxUrl -OutFile $jfxZip

Write-Host "Extracting JavaFX SDK..."
Expand-Archive -Path $jfxZip -DestinationPath . -Force

Write-Host "Moving JavaFX jars and dlls to lib/javafx..."
Copy-Item -Path "javafx-sdk-22.0.1\lib\*.jar" -Destination $javafxDir -Force
Copy-Item -Path "javafx-sdk-22.0.1\bin\*.dll" -Destination $javafxDir -Force

Write-Host "Cleaning up JavaFX zip and extracted folder..."
Remove-Item -Path "javafx-sdk-22.0.1" -Recurse -Force
Remove-Item -Path $jfxZip -Force

Write-Host "All JARs downloaded and configured successfully in lib subfolders."
