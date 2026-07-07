import { test, expect } from '@playwright/test'

// 串行:共享后端状态,顺序有意义(空态须在任何创建之前)。
test.describe.configure({ mode: 'serial' })

test('三态·空:无讨论时首页显示空台占位', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText('暂无进行中的圆桌', { exact: false })).toBeVisible()
})

test('三态·加载:首页接入时显示信号接入中', async ({ page }) => {
  // 延迟列表接口,捕获加载态
  await page.route('**/api/discussions', async (route) => {
    if (route.request().method() === 'GET') {
      await new Promise((r) => setTimeout(r, 1200))
    }
    await route.continue()
  })
  await page.goto('/')
  await expect(page.getByText('信号接入中', { exact: false })).toBeVisible()
})

test('主链路:发起→生成→确认→观看实时→共识分歧→收尾总结', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: /发起新讨论/ }).click()
  await expect(page).toHaveURL(/\/new/)

  await page.getByPlaceholder(/输入任意议题/).fill('AI 会重塑教育吗?')
  await page.getByRole('button', { name: /生成阵容/ }).click()

  // 阵容出现:主持人 + 4 专家 = 5 张卡
  await expect(page.locator('.avatar-card')).toHaveCount(5)

  await page.getByRole('button', { name: /确认阵容/ }).click()
  await expect(page).toHaveURL(/\/discussions\/\d+/)

  // 实时/续看:transcript 出现发言
  await expect(page.locator('.speech-nameplate').first()).toBeVisible()
  // 共识 / 分歧出现
  await expect(page.locator('.ticker-lane .lane-row').first()).toBeVisible()
  // 收尾:主持人自然语言总结
  await expect(page.locator('.summary-panel__text')).toBeVisible()
  const summary = (await page.locator('.summary-panel__text').innerText()).trim()
  expect(summary.length).toBeGreaterThan(5)

  // 红线:页面无 JSON 原文、无内部事件字段
  const body = await page.locator('body').innerText()
  expect(body).not.toContain('reactionType')
  expect(body).not.toContain('speakerId')
  expect(body).not.toContain('"content"')
  // transcript 不显示内部事件类型词
  const transcript = await page.locator('.studio__speeches').innerText()
  expect(transcript).not.toContain('举手')
  expect(transcript).not.toContain('反驳')
})

test('多讨论并行隔离:两讨论各自跑、演播厅各显其题', async ({ page }) => {
  // 经代理用 API 建两讨论并确认(快),再在 UI 逐个核对隔离
  const create = async (topic: string) => {
    const res = await page.request.post('/api/discussions', { data: { topic, expertCount: 3 } })
    const id = (await res.json()).id as number
    await page.request.post(`/api/discussions/${id}/confirm`)
    return id
  }
  const a = await create('隔离讨论·甲')
  const b = await create('隔离讨论·乙')

  await page.goto(`/discussions/${a}`)
  await expect(page.locator('.studio__topic')).toContainText('隔离讨论·甲')
  await expect(page.locator('.speech-nameplate').first()).toBeVisible()

  await page.goto(`/discussions/${b}`)
  await expect(page.locator('.studio__topic')).toContainText('隔离讨论·乙')
  await expect(page.locator('.speech-nameplate').first()).toBeVisible()
})

test('三态·错误:阵容生成失败渲染错误态 + 重试', async ({ page }) => {
  await page.goto('/new')
  await page.getByPlaceholder(/输入任意议题/).fill('__FAIL__ 触发失败')
  await page.getByRole('button', { name: /生成阵容/ }).click()
  await expect(page.getByText('阵容生成失败', { exact: false })).toBeVisible()
  await expect(page.getByRole('button', { name: /重试/ })).toBeVisible()
})
