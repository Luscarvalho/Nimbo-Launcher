#!/usr/bin/env python3
"""Build a dependency-free Android 9+ launcher using an installed Android SDK and JDK."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, zipfile

ROOT = Path(__file__).resolve().parent
SDK = Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'Library/Android/sdk')))
TOOLS = SDK / 'build-tools' / os.environ.get('ANDROID_BUILD_TOOLS', '37.0.0')
JAR = SDK / 'platforms/android-34/android.jar'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--unsigned', action='store_true', help='Compile without the private signing key; not an installable update')
args = parser.parse_args()
key = ROOT / 'home-simples.jks'
expected_file = ROOT / 'signing-certificate.sha256'
if not args.unsigned:
    if not key.is_file() or not expected_file.is_file():
        raise SystemExit('Chave/certificado de atualização ausente. Restaure a chave original; não gere outra para atualizar a TV. Use --unsigned somente para verificar a compilação.')
    password = os.environ.get('NIMBO_KEYSTORE_PASSWORD')
    if not password:
        config = ROOT / 'signing.local.json'
        if config.is_file():
            try:
                password = json.loads(config.read_text()).get('keystore_password')
            except (OSError, ValueError, AttributeError):
                raise SystemExit('Configuração de assinatura inválida em signing.local.json.')
    if not isinstance(password, str) or not password:
        raise SystemExit('Senha de assinatura ausente. Defina NIMBO_KEYSTORE_PASSWORD ou configure signing.local.json. Use --unsigned para verificar a compilação.')
    os.environ['NIMBO_KEYSTORE_PASSWORD'] = password
    certificate = subprocess.run(['keytool', '-exportcert', '-keystore', str(key), '-storepass:env',
        'NIMBO_KEYSTORE_PASSWORD', '-alias', 'home-simples'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True).stdout
    if hashlib.sha256(certificate).hexdigest() != expected_file.read_text().strip().lower():
        raise SystemExit('A chave não corresponde ao certificado desta instalação. Compilação interrompida.')
BUILD = ROOT / 'build'
if BUILD.exists(): shutil.rmtree(BUILD)
for folder in ['classes', 'dex', 'generated/com/nimbo/launcher']:
    (BUILD / folder).mkdir(parents=True)
apps = [line.strip() for line in (ROOT/'apps.txt').read_text().splitlines() if line.strip() and not line.startswith('#')]
(BUILD/'generated/com/nimbo/launcher/Apps.java').write_text('package com.nimbo.launcher;\nfinal class Apps { static final String[] LIST = {' + ','.join(json.dumps(x) for x in apps) + '}; }\n')

def run(*args): subprocess.run([str(x) for x in args], cwd=ROOT, check=True)

run(TOOLS/'aapt2', 'compile', '--dir', ROOT/'res', '-o', BUILD/'resources.zip')
run(TOOLS/'aapt2', 'link', '-o', BUILD/'base.apk', '-I', JAR, '--manifest', ROOT/'AndroidManifest.xml',
    '--min-sdk-version', '28', '--target-sdk-version', '34', '--java', BUILD/'generated', BUILD/'resources.zip')
sources = sorted((ROOT/'src').rglob('*.java')) + sorted((BUILD/'generated').rglob('*.java'))
run('javac', '-Xlint:deprecation', '-Xlint:-options', '-source', '1.8', '-target', '1.8', '-bootclasspath',
    str(JAR)+os.pathsep+str(TOOLS/'core-lambda-stubs.jar'), '-d', BUILD/'classes', *sources)
run(TOOLS/'d8', '--min-api', '28', '--release', '--lib', JAR, '--output', BUILD/'dex', *sorted((BUILD/'classes').rglob('*.class')))
shutil.copy(BUILD/'base.apk', BUILD/'unsigned.apk')
with zipfile.ZipFile(BUILD/'unsigned.apk','a',compression=zipfile.ZIP_DEFLATED) as z:
    z.write(BUILD/'dex/classes.dex','classes.dex')
run(TOOLS/'zipalign', '-f', '4', BUILD/'unsigned.apk', BUILD/'aligned.apk')
if args.unsigned:
    output = BUILD/'nimbo-unsigned.apk'
    shutil.copy(BUILD/'aligned.apk', output)
else:
    output = BUILD/'nimbo.apk'
    run(TOOLS/'apksigner','sign','--ks',key,'--ks-pass','env:NIMBO_KEYSTORE_PASSWORD','--out',output,BUILD/'aligned.apk')
    run(TOOLS/'apksigner','verify',output)
print('Built:', output)
