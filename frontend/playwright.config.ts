import { defineConfig, devices } from '@playwright/test'

// E2E:确定性 fake-ai 后端(空库、零 Deepseek 花费)+ vite 前端(/api 代理)。串行执行,共享后端状态。
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 45_000,
  expect: { timeout: 20_000 },
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      // 后端:fake-ai + 空库(禁 seed,便于测空态);每次清库保新鲜
      command:
        'sh -c "cd ../backend && rm -f ../db/e2e-panel.db* && SPRING_PROFILES_ACTIVE=fake-ai DB_PATH=../db/e2e-panel.db SPRING_SQL_INIT_DATA_LOCATIONS= mvn -q -B spring-boot:run"',
      url: 'http://localhost:8080/api/discussions',
      timeout: 180_000,
      reuseExistingServer: false,
    },
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      timeout: 60_000,
      reuseExistingServer: false,
    },
  ],
})
