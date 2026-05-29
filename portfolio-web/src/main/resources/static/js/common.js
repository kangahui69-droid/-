// ========== TechForge 公共函数 ==========

// ========== 工具函数 ==========

/**
 * 日期格式化 - 显示友好的相对时间
 * @param {string} dateStr - ISO 日期字符串
 * @returns {string} 格式化后的日期
 */
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) {
        // 如果解析失败，返回原始格式
        return dateStr;
    }
    const now = new Date();
    const diff = now - date;

    // 1分钟内
    if (diff < 60000) {
        return '刚刚';
    }
    // 1小时内
    if (diff < 3600000) {
        const mins = Math.floor(diff / 60000);
        return mins + '分钟前';
    }
    // 24小时内
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return hours + '小时前';
    }
    // 7天内
    if (diff < 604800000) {
        const days = Math.floor(diff / 86400000);
        return days + '天前';
    }
    // 正常日期格式
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

/**
 * HTML 转义（防止 XSS）
 * @param {string} text - 要转义的文本
 * @returns {string} 转义后的文本
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ========== 导航和页脚 ==========

/**
 * 加载导航栏到指定容器
 */
async function loadNav(containerId = 'navContainer') {
    const container = document.getElementById(containerId);
    if (!container) {
        console.warn('导航容器 #' + containerId + ' 不存在');
        return;
    }

    container.innerHTML = `
        <div class="nav-links">
            <a class="nav-link" href="/">首页</a>
            <a class="nav-link" href="/articles.html">文章</a>
            <a class="nav-link" href="/projects.html">项目</a>
            <a class="nav-link" href="/skills.html">技能</a>
            <a class="nav-link" href="/chat.html">AI 对话</a>
        </div>
    `;
}

/**
 * 加载页脚到指定容器
 */
async function loadFooter(containerId = 'footerContainer') {
    const container = document.getElementById(containerId);
    if (!container) {
        console.warn('页脚容器 #' + containerId + ' 不存在');
        return;
    }

    // 获取当前年份
    const year = new Date().getFullYear();

    container.innerHTML = `
        <div class="footer">
            <p>&copy; ${year} TechForge · AI 开发者</p>
        </div>
    `;
}

/**
 * 初始化通用组件（导航 + 页脚）
 */
async function initCommon() {
    await Promise.all([loadNav(), loadFooter()]);
}

// ========== API 请求 ==========

/**
 * 发送 GET 请求
 * @param {string} url - 请求 URL
 * @returns {Promise<object>} 响应数据
 */
async function apiGet(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('HTTP ' + response.status);
    }
    return response.json();
}

/**
 * 发送 POST 请求
 * @param {string} url - 请求 URL
 * @param {object} data - 请求体数据
 * @returns {Promise<object>} 响应数据
 */
async function apiPost(url, data) {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error('HTTP ' + response.status);
    }
    return response.json();
}