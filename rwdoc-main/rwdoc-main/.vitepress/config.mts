import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  srcDir: "docs",
  ignoreDeadLinks: true,
  title: "Redwhite docs",
  head: [['link', { rel: 'icon', href: '/favicon.png' }]],
  lang: 'zh-CN',
  description: "是红白阁的文档库喵",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    logo: '/logo.png',
    nav: [
      { text: '首页', link: '/' },
      { text: '游玩教程', link: '/noew'},
      { text: '关于酿酒', link: '/brew'},
      { text: '加入服务器群', link: 'https://qm.qq.com/q/YVe5PqGvm4'},
      { text: '更新日志', link: '/updatelog' }
    ],
    footer: {
      message: '备案号：<a href="https://beian.miit.gov.cn/" target="_blank">鲁ICP备2024127829号</a>',
      copyright: 'Redwhite © 2025'
    },

    sidebar: [
      {
        text: '游玩教程',
        items: [
          { text: '注册教程', link: '/regisbs'},
          { text: '游玩教程', link: '/noew'},
          { text: '关于酿酒', link: '/brew'},
          { text: '不同版本的Minecraft的区别？', link: '/diffmc' }
        ]
      },
      {
        text: '服务器信息',
        items: [
          { text: '更新日志', link: '/updatelog' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/RWPteam' }
    ]
  }
})
