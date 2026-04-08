const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

const repoRoot = path.resolve(__dirname, '..');
const backendDir = path.join(repoRoot, 'backend');
const logDir = process.env.HMS_BACKEND_RUN_LOG_DIR
  ? path.resolve(repoRoot, process.env.HMS_BACKEND_RUN_LOG_DIR)
  : path.join(repoRoot, 'logs', 'backend');

fs.mkdirSync(logDir, { recursive: true });

const outLog = fs.createWriteStream(path.join(logDir, 'backend-run.out.log'), { flags: 'a' });
const errLog = fs.createWriteStream(path.join(logDir, 'backend-run.err.log'), { flags: 'a' });
const combinedLog = fs.createWriteStream(path.join(logDir, 'backend-run.log'), { flags: 'a' });

const mvnw = process.platform === 'win32' ? 'mvnw.cmd' : './mvnw';
const child = spawn(mvnw, ['spring-boot:run'], {
  cwd: backendDir,
  env: {
    ...process.env,
    HMS_LOG_PATH: process.env.HMS_LOG_PATH || path.join(repoRoot, 'logs', 'backend'),
  },
  shell: process.platform === 'win32',
});

child.stdout.pipe(process.stdout);
child.stdout.pipe(outLog);
child.stdout.pipe(combinedLog);

child.stderr.pipe(process.stderr);
child.stderr.pipe(errLog);
child.stderr.pipe(combinedLog);

child.on('exit', (code, signal) => {
  outLog.end();
  errLog.end();
  combinedLog.end();

  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code || 0);
});

