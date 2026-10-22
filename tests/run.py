#!/usr/bin/env python3
"""Run meaningful JVM and release-key regressions without a TV, emulator or extra dependencies."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='home-simples-tests-') as folder:
    temp = Path(folder)
    sources = [ROOT/'src/com/nimbo/launcher'/name for name in ('AppOrder.java', 'LatestTaskRunner.java')]
    sources += list((ROOT/'tests/com').rglob('*.java'))
    subprocess.run(['javac', '-d', str(temp/'classes'), *map(str, sources)], check=True)
    subprocess.run(['java', '-cp', str(temp/'classes'), 'com.nimbo.launcher.RegressionTests'], check=True)

    # A missing signing key must fail before deleting an existing build or creating a replacement key.
    project = temp/'missing-key'
    project.mkdir()
    shutil.copy2(ROOT/'build.py', project/'build.py')
    (project/'build').mkdir()
    sentinel = project/'build/keep-this-file'
    sentinel.write_text('preserved')
    result = subprocess.run(['python3', str(project/'build.py')], capture_output=True, text=True)
    assert result.returncode != 0 and 'ausente' in result.stderr
    assert sentinel.read_text() == 'preserved' and not (project/'home-simples.jks').exists()
    print('PASS: missing key refuses release and preserves existing build')

    # A missing password must fail without invoking keytool or replacing the existing build.
    (project/'home-simples.jks').write_bytes(b'not-a-real-keystore')
    (project/'signing-certificate.sha256').write_text('0' * 64)
    env = {name: value for name, value in os.environ.items() if name != 'NIMBO_KEYSTORE_PASSWORD'}
    result = subprocess.run(['python3', str(project/'build.py')], capture_output=True, text=True, env=env)
    assert result.returncode != 0 and 'Senha de assinatura ausente' in result.stderr
    assert sentinel.read_text() == 'preserved'
    print('PASS: missing password refuses release and preserves existing build')

    # The expected public certificate is checked before any build output is replaced.
    if (ROOT/'home-simples.jks').is_file():
        shutil.copy2(ROOT/'home-simples.jks', project/'home-simples.jks')
        (project/'home-simples.jks').chmod(0o600)
        if (ROOT/'signing.local.json').is_file():
            shutil.copy2(ROOT/'signing.local.json', project/'signing.local.json')
            (project/'signing.local.json').chmod(0o600)
        (project/'signing-certificate.sha256').write_text('0' * 64)
        result = subprocess.run(['python3', str(project/'build.py')], capture_output=True, text=True)
        assert result.returncode != 0 and 'não corresponde' in result.stderr
        assert sentinel.read_text() == 'preserved'
        print('PASS: mismatching certificate refuses release and preserves existing build')
    else:
        print('SKIP: certificate mismatch test needs the local signing key')
