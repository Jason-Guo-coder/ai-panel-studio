#!/bin/bash
# AI 圆桌讨论 · 一键启动(双击运行)
# 自动启动后端(自动建库+灌 seed)+ 前端,并打开首页;Ctrl+C 或关闭窗口即停止并清理。
set -u

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
RUN_DIR="$ROOT/.run"
mkdir -p "$RUN_DIR"
BACKEND_LOG="$RUN_DIR/backend.log"
FRONTEND_LOG="$RUN_DIR/frontend.log"
BACK_PID=""
FRONT_PID=""

cleanup() {
  trap - INT TERM HUP EXIT      # 防止重复触发
  echo ""
  echo "🧹 正在关闭并清理…"
  [ -n "${TAIL_PID:-}" ]  && kill "$TAIL_PID"  2>/dev/null
  [ -n "$FRONT_PID" ] && kill "$FRONT_PID" 2>/dev/null
  [ -n "$BACK_PID" ]  && kill "$BACK_PID"  2>/dev/null
  pkill -f "spring-boot:run" 2>/dev/null
  pkill -f "vite" 2>/dev/null
  lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null
  lsof -ti:5173 2>/dev/null | xargs kill -9 2>/dev/null
  # 清缓存垃圾(保留数据库 panel.db 与依赖 node_modules/target)
  rm -rf "$RUN_DIR"
  rm -f "$ROOT"/db/*.db-wal "$ROOT"/db/*.db-shm 2>/dev/null
  rm -rf "$ROOT"/frontend/node_modules/.vite 2>/dev/null
  echo "✅ 已关闭并清理完毕。"
  exit 0
}
trap cleanup INT TERM HUP EXIT

echo "════════════════════════════════════════════"
echo "  AI 圆桌讨论 · 一键启动"
echo "════════════════════════════════════════════"

# 依赖检查
for c in java mvn node npm curl; do
  command -v "$c" >/dev/null 2>&1 || { echo "❌ 缺少依赖:$c,请先安装后重试。"; exit 1; }
done

# 环境变量:有 key 用真实 Deepseek,否则用内置确定性 AI(fake-ai,零花费仍可玩)
PROFILE_ENV=""
if [ -f backend/.env ]; then set -a; . ./backend/.env; set +a; fi
if [ -z "${DEEPSEEK_API_KEY:-}" ]; then
  echo "⚠️  未检测到 DEEPSEEK_API_KEY —— 使用内置确定性 AI(fake-ai)启动,零花费即可体验。"
  echo "    如需真实大模型:cp backend/.env.example backend/.env 并填入 DEEPSEEK_API_KEY,再重启本脚本。"
  PROFILE_ENV="fake-ai"
fi

echo "🚀 启动后端(首次会编译,约 10–30s)…"
( cd backend && SPRING_PROFILES_ACTIVE="$PROFILE_ENV" mvn -q -B spring-boot:run > "$BACKEND_LOG" 2>&1 ) &
BACK_PID=$!
UP=""
for i in $(seq 1 90); do
  curl -s -o /dev/null http://localhost:8080/api/discussions 2>/dev/null && { UP=1; break; }
  sleep 2
done
[ -z "$UP" ] && { echo "❌ 后端启动超时,见 $BACKEND_LOG"; cleanup; }
echo "   ✓ 后端就绪:http://localhost:8080"

echo "🚀 启动前端…"
( cd frontend && { [ -d node_modules ] || npm install; } && npm run dev > "$FRONTEND_LOG" 2>&1 ) &
FRONT_PID=$!
for i in $(seq 1 90); do
  curl -s -o /dev/null http://localhost:5173 2>/dev/null && break
  sleep 1
done
echo "   ✓ 前端就绪:http://localhost:5173"

echo "🌐 打开首页…"
open http://localhost:5173 2>/dev/null

echo ""
echo "════════════════════════════════════════════"
echo "  ✅ 已启动:http://localhost:5173"
echo "  按 Ctrl+C 或关闭本窗口 → 自动停止并清理"
echo "════════════════════════════════════════════"
echo "（以下为实时日志）"
echo ""

# 保持窗口常驻 + 实时日志;tail 后台 + wait,使 Ctrl+C/关窗能及时触发 cleanup
tail -f "$BACKEND_LOG" "$FRONTEND_LOG" &
TAIL_PID=$!
wait "$TAIL_PID"
