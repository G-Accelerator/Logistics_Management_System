const fs = require("fs");
const inPath = require("path").join(
  __dirname,
  "..",
  "系统实现文档.md",
);
const outPath = require("path").join(
  __dirname,
  "..",
  "毕业论文-系统实现说明.txt",
);
let s = fs.readFileSync(inPath, "utf8");
s = s.replace(/\r\n/g, "\n");

// bold
s = s.replace(/\*\*([^*]+)\*\*/g, "$1");

// fenced code: mermaid -> 文字说明；其它代码块保留为缩进文本
s = s.replace(/```mermaid[\s\S]*?```/g, "\n【图示说明】系统功能分为四层：认证与会话（账号密码登录、手机号验证码登录、登出与 Token）、核心业务（订单 CRUD、发货签收取消与批量、站点到达模拟、运单号与轨迹 JSON、Excel 导入导出）、分析与展示（首页统计与最近订单、统计报表与 ECharts、物流跟踪地图与时间线）、配置（快递公司档案、高德路线规划）；配置支撑业务，业务产生数据供分析层展示。\n");

s = s.replace(/```[\s\S]*?```/g, (block) => {
  const inner = block.replace(/^```\w*\n?/, "").replace(/\n?```$/, "");
  if (inner.length < 200) return "\n" + inner.trim() + "\n";
  return "\n【代码/配置摘录】\n" + inner.trim().split("\n").slice(0, 15).join("\n") + "\n……\n";
});

// headings
s = s.replace(/^#### (.*)$/gm, "$1");
s = s.replace(/^### (.*)$/gm, "\n$1\n");
s = s.replace(/^## (.*)$/gm, "\n\n$1\n" + "─".repeat(36) + "\n");
s = s.replace(/^# (.*)$/gm, "\n\n\n$1\n" + "═".repeat(40) + "\n");

// inline backticks
s = s.replace(/`([^`]+)`/g, "$1");

/**
 * 将 Markdown 管道表格转为「制表符分隔」文本，便于 Word：
 * 选中整块（含表头行）→ 插入 → 表格 → 文本转换成表格 → 分隔符选「制表符」。
 */
function markdownTablesToTsv(text) {
  const lines = text.split("\n");
  const out = [];
  let i = 0;
  while (i < lines.length) {
    const raw = lines[i];
    const trimmed = raw.trim();
    const looksLikeRow =
      trimmed.startsWith("|") &&
      trimmed.endsWith("|") &&
      (trimmed.match(/\|/g) || []).length >= 2;

    if (!looksLikeRow) {
      out.push(raw);
      i++;
      continue;
    }

    const block = [];
    let j = i;
    while (j < lines.length) {
      const t = lines[j].trim();
      if (!(t.startsWith("|") && t.endsWith("|") && (t.match(/\|/g) || []).length >= 2)) {
        break;
      }
      block.push(lines[j]);
      j++;
    }

    const hasSeparator = block.some((row) =>
      /^\|[\s\-:|]+\|\s*$/.test(row.trim()),
    );

    if (block.length < 2 || !hasSeparator) {
      for (const b of block) out.push(b);
      i = j;
      continue;
    }

    out.push("");
    for (const row of block) {
      const tr = row.trim();
      if (/^\|[\s\-:|]+\|\s*$/.test(tr)) continue;
      const cells = tr
        .slice(1, -1)
        .split("|")
        .map((c) => c.trim().replace(/\s+/g, " "));
      out.push(cells.join("\t"));
    }
    out.push("");
    i = j;
  }
  return out.join("\n");
}

s = markdownTablesToTsv(s);

// horizontal rules（须在表格转换之后，避免误改表内内容）
s = s.replace(/^---$/gm, "────────────────────────────────────────");

// trim trailing spaces per line
s = s
  .split("\n")
  .map((line) => line.replace(/[ \t]+$/, ""))
  .join("\n");

// 文首说明（便于论文使用）
const header =
  "【使用说明】本文档由《系统实现文档》自动转换为纯文本，可直接全选复制到 Microsoft Word 或 WPS 中，" +
  "再统一设置「标题1/标题2/正文」样式。\n\n" +
  "【表格转成 Word】文中表格已转为「制表符（Tab）分隔」的若干行：用鼠标选中从表头到表尾的所有行（仅表格行，不含上下空行），" +
  "菜单「插入」→「表格」→「文本转换成表格」→ 分隔符选「制表符」→ 确定。列宽可在表格属性中微调。\n\n";

fs.writeFileSync(outPath, header + s.trim() + "\n", "utf8");
console.log("Written:", outPath);
