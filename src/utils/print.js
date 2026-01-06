/**
 * 打印工具函数
 * 提供统一的PDF下载功能，将页面内容转换为PDF并下载
 */

import html2pdf from 'html2pdf.js'

/**
 * 下载PDF
 * @param {Object} options 下载选项
 * @param {Function} options.beforePrint - 下载前的回调函数，用于准备数据
 * @param {string} options.selector - 要下载的元素选择器，默认为当前页面
 * @param {number} options.delay - 延迟时间（毫秒），默认500ms
 * @param {Function} options.onError - 错误回调函数
 * @param {string} options.filename - PDF文件名（可选）
 */
export function printPage(options = {}) {
  const {
    beforePrint = null,
    selector = null,
    delay = 500,
    onError = null,
    filename = null
  } = options

  console.log('=== 开始PDF下载流程 ===')

  // 执行下载前的准备工作
  if (beforePrint && typeof beforePrint === 'function') {
    try {
      const result = beforePrint()
      // 如果返回Promise，等待完成
      if (result && typeof result.then === 'function') {
        result.then(() => {
          executeDownload(selector, delay, onError, filename)
        }).catch((error) => {
          console.error('下载前准备失败:', error)
          if (onError) onError(error)
        })
        return
      }
    } catch (error) {
      console.error('下载前准备失败:', error)
      if (onError) onError(error)
      return
    }
  }

  executeDownload(selector, delay, onError, filename)
}

/**
 * 执行PDF下载
 */
function executeDownload(selector, delay, onError, filename) {
  // 等待DOM更新
  setTimeout(() => {
    let clonedElement = null
    
    try {
      
      let element = null
      
      // 如果指定了选择器，检查元素是否存在
      if (selector) {
        element = document.querySelector(selector)
        if (!element) {
          console.warn(`未找到打印元素: ${selector}，将打印整个页面`)
          element = document.body
        } else {
          console.log('✓ 找到打印元素:', selector)
          console.log('元素类名:', element.className)
          console.log('元素内容长度:', element.innerHTML.length)
        }
      } else {
        // 没有选择器，使用整个 body
        element = document.body
        console.log('未指定选择器，打印整个页面')
      }

      // 确保 element 存在
      if (!element) {
        element = document.body
      }
      
      // 生成文件名
      const defaultFilename = filename || generateFilename(selector)
      
      // 保存需要恢复的样式
      const elementsToRestore = []
      const allElements = element.querySelectorAll('*')
      allElements.forEach(el => {
        const computedStyle = window.getComputedStyle(el)
        if (computedStyle.display === 'none' || computedStyle.visibility === 'hidden') {
          elementsToRestore.push({
            element: el,
            display: el.style.display || '',
            visibility: el.style.visibility || ''
          })
        }
      })
      
      // 保存元素的原始样式
      const originalElementStyles = {
        display: element.style.display || '',
        visibility: element.style.visibility || '',
        position: element.style.position || '',
        left: element.style.left || '',
        top: element.style.top || '',
        zIndex: element.style.zIndex || ''
      }
      
      // 临时应用样式以确保内容可见（用于PDF生成）
      // 将元素移到可见位置，但不影响页面布局
      element.style.position = 'relative'
      element.style.left = '0'
      element.style.top = '0'
      element.style.zIndex = '9999'
      element.style.display = 'block'
      element.style.visibility = 'visible'
      element.style.width = 'auto'
      element.style.maxWidth = '800px'
      element.style.background = 'white'
      element.style.color = '#000'
      
      // 应用打印样式
      applyStylesToElement(element)
      
      // 清理函数
      const cleanup = () => {
        // 恢复元素的原始样式
        Object.keys(originalElementStyles).forEach(key => {
          const prop = key.replace(/([A-Z])/g, '-$1').toLowerCase()
          if (originalElementStyles[key]) {
            element.style[prop] = originalElementStyles[key]
          } else {
            element.style.removeProperty(prop)
          }
        })
        
        // 恢复子元素的样式
        elementsToRestore.forEach(item => {
          if (item.display) {
            item.element.style.display = item.display
          } else {
            item.element.style.removeProperty('display')
          }
          if (item.visibility) {
            item.element.style.visibility = item.visibility
          } else {
            item.element.style.removeProperty('visibility')
          }
        })
      }
      
      // 等待样式应用和DOM更新，然后生成PDF
      setTimeout(() => {
        // 检查元素是否有内容
        const hasContent = element.textContent && element.textContent.trim().length > 0
        const hasTables = element.querySelectorAll('table').length > 0
        const tables = element.querySelectorAll('table')
        const rows = element.querySelectorAll('tbody tr')
        
        console.log('元素检查:')
        console.log('- 是否有文本内容:', hasContent)
        console.log('- 是否有表格:', hasTables)
        console.log('- 表格数量:', tables.length)
        console.log('- 表格行数:', rows.length)
        console.log('- 元素HTML长度:', element.innerHTML.length)
        console.log('- 元素尺寸:', {
          width: element.offsetWidth,
          height: element.offsetHeight,
          scrollWidth: element.scrollWidth,
          scrollHeight: element.scrollHeight
        })
        
        if (!hasContent && !hasTables) {
          console.warn('⚠️ 警告：元素似乎没有内容，PDF可能为空')
        }
        
        generatePDF(element, defaultFilename, (error) => {
          // PDF生成完成后，恢复样式
          cleanup()
          if (onError && error) {
            onError(error)
          }
        })
      }, 800) // 增加等待时间，确保样式完全应用
    } catch (error) {
      console.error('执行PDF下载时发生错误:', error)
      // 发生错误时也要清理克隆元素
      if (clonedElement && clonedElement.parentNode) {
        try {
          clonedElement.parentNode.removeChild(clonedElement)
        } catch (e) {
          console.warn('清理克隆元素失败:', e)
        }
      }
      if (onError) onError(error)
      else alert('PDF下载失败: ' + error.message)
    }
  }, delay)
}

/**
 * 对元素应用打印样式
 */
function applyStylesToElement(element) {
  if (!element) return
  
  // 设置元素基本样式
  element.style.display = 'block'
  element.style.visibility = 'visible'
  element.style.opacity = '1'
  element.style.color = '#000'
  element.style.background = 'white'
  element.style.position = 'relative'
  element.style.width = 'auto'
  element.style.height = 'auto'
  
  // 隐藏不需要的元素
  const buttons = element.querySelectorAll('.btn, button')
  buttons.forEach(btn => {
    btn.style.display = 'none'
  })
  
  const pageHeaders = element.querySelectorAll('.page-header')
  pageHeaders.forEach(header => {
    header.style.display = 'none'
  })
  
  // 隐藏订单明细列表
  const orderDetails = element.querySelectorAll('.order-details-section')
  orderDetails.forEach(detail => {
    detail.style.display = 'none'
  })
  
  // 隐藏表单元素
  const formElements = element.querySelectorAll('.form-control, .form-select, .form-label, input[type="month"], input[type="date"], select')
  formElements.forEach(el => {
    el.style.display = 'none'
  })
  
  // 确保所有文本元素可见
  const allTextElements = element.querySelectorAll('p, span, div, h1, h2, h3, h4, h5, h6, label')
  allTextElements.forEach(el => {
    const computedStyle = window.getComputedStyle(el)
    if (computedStyle.display === 'none') {
      // span 保持 inline，其他保持 block
      if (el.tagName.toLowerCase() === 'span') {
        el.style.display = 'inline'
      } else {
        el.style.display = 'block'
      }
    }
    if (computedStyle.visibility === 'hidden') {
      el.style.visibility = 'visible'
    }
    el.style.color = '#000'
    // 修复渐变文字在打印时不可见的问题
    if (el.style.webkitTextFillColor === 'transparent' || 
        computedStyle.webkitTextFillColor === 'transparent') {
      el.style.webkitTextFillColor = '#000'
    }
    // 移除渐变背景
    if (el.style.backgroundImage && el.style.backgroundImage.includes('gradient')) {
      el.style.backgroundImage = 'none'
    }
  })
  
  // 确保表格可见
  const tables = element.querySelectorAll('table')
  tables.forEach(table => {
    table.style.display = 'table'
    table.style.visibility = 'visible'
    table.style.width = '100%'
    table.style.borderCollapse = 'collapse'
  })
  
  // 确保表格元素可见
  const theads = element.querySelectorAll('thead')
  theads.forEach(thead => {
    thead.style.display = 'table-header-group'
    thead.style.visibility = 'visible'
  })
  
  const tbodies = element.querySelectorAll('tbody')
  tbodies.forEach(tbody => {
    tbody.style.display = 'table-row-group'
    tbody.style.visibility = 'visible'
  })
  
  const tfoots = element.querySelectorAll('tfoot')
  tfoots.forEach(tfoot => {
    tfoot.style.display = 'table-footer-group'
    tfoot.style.visibility = 'visible'
  })
  
  const trs = element.querySelectorAll('tr')
  trs.forEach(tr => {
    tr.style.display = 'table-row'
    tr.style.visibility = 'visible'
  })
  
  const ths = element.querySelectorAll('th')
  ths.forEach(th => {
    th.style.display = 'table-cell'
    th.style.visibility = 'visible'
    th.style.color = '#000'
    th.style.border = '1px solid #333'
    th.style.padding = '8px'
  })
  
  const tds = element.querySelectorAll('td')
  tds.forEach(td => {
    td.style.display = 'table-cell'
    td.style.visibility = 'visible'
    td.style.color = '#000'
    td.style.border = '1px solid #333'
    td.style.padding = '8px'
  })
}

/**
 * 生成PDF并下载
 */
function generatePDF(element, filename, onError) {
  try {
    console.log('开始生成PDF，文件名:', filename)
    
    const opt = {
      margin: [10, 10, 10, 10],
      filename: filename,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { 
        scale: 2,
        useCORS: true,
        logging: false, // 关闭日志
        backgroundColor: '#ffffff',
        windowWidth: element.scrollWidth || 800,
        windowHeight: element.scrollHeight || 600,
        allowTaint: true,
        removeContainer: false,
        onclone: (clonedDoc) => {
          try {
            // 在克隆的文档中强制所有颜色为黑白
            const clonedElement = clonedDoc.querySelector('.employee-order-report')
            if (!clonedElement) {
              console.warn('未找到 .employee-order-report 元素')
              return
            }
            
            // 强制所有文字为黑色，移除所有彩色背景
            const allElements = clonedElement.querySelectorAll('*')
            allElements.forEach(el => {
              el.style.color = '#000'
              el.style.webkitTextFillColor = '#000'
              el.style.visibility = 'visible'
              el.style.opacity = '1'
              
              // 移除所有渐变和彩色背景
              el.style.backgroundImage = 'none'
              
              // 移除所有彩色背景色，只保留白色或浅灰色
              const tagName = el.tagName
              const className = el.className || ''
              
              if (tagName === 'TH' || className.includes('summary-table') && tagName === 'TH') {
                el.style.backgroundColor = '#f5f5f5'
              } else if (tagName === 'TD') {
                el.style.backgroundColor = '#ffffff'
              } else if (className.includes('total-row')) {
                el.style.backgroundColor = '#f0f0f0'
              } else if (className.includes('report-info') || className.includes('card-header') || className.includes('card-body')) {
                el.style.backgroundColor = '#ffffff'
              } else {
                // 其他元素设为透明或白色
                el.style.backgroundColor = 'transparent'
              }
            })
            
            // 确保表格可见
            const tables = clonedElement.querySelectorAll('table')
            tables.forEach(table => {
              table.style.display = 'table'
              table.style.visibility = 'visible'
              table.style.width = '100%'
            })
            
            // 确保表格元素可见
            const rows = clonedElement.querySelectorAll('tr')
            rows.forEach(tr => {
              tr.style.display = 'table-row'
              tr.style.visibility = 'visible'
            })
            
            const cells = clonedElement.querySelectorAll('td, th')
            cells.forEach(cell => {
              cell.style.display = 'table-cell'
              cell.style.visibility = 'visible'
              cell.style.color = '#000'
              cell.style.border = '1px solid #000'
            })
            
            // 表头背景
            const headers = clonedElement.querySelectorAll('th')
            headers.forEach(th => {
              th.style.backgroundColor = '#f5f5f5'
            })
            
            // 合计行背景
            const totalRows = clonedElement.querySelectorAll('.total-row')
            totalRows.forEach(tr => {
              tr.style.backgroundColor = '#f0f0f0'
            })
            
            // 确保文本元素可见
            const textElements = clonedElement.querySelectorAll('span, p, div, h1, h2, h3, h4, h5, h6')
            textElements.forEach(el => {
              el.style.color = '#000'
              el.style.webkitTextFillColor = '#000'
              el.style.visibility = 'visible'
              if (el.tagName === 'SPAN') {
                el.style.display = 'inline'
              }
            })
          } catch (error) {
            console.warn('onclone 处理出错，但不影响PDF生成:', error)
          }
        }
      },
      jsPDF: { 
        unit: 'mm', 
        format: 'a4', 
        orientation: 'portrait',
        compress: true
      },
      pagebreak: { mode: ['avoid-all', 'css', 'legacy'] }
    }
    
    console.log('PDF配置:', opt)
    console.log('元素尺寸:', {
      width: element.offsetWidth,
      height: element.offsetHeight,
      scrollWidth: element.scrollWidth,
      scrollHeight: element.scrollHeight
    })
    console.log('元素内容预览:', element.textContent?.substring(0, 200))
    
    html2pdf()
      .set(opt)
      .from(element)
      .save()
      .then(() => {
        console.log('✓ PDF下载成功')
        // PDF下载成功后，调用回调（会清理克隆元素）
        if (onError && typeof onError === 'function') {
          onError(null) // 成功时传递null
        }
      })
      .catch((error) => {
        console.error('PDF生成失败:', error)
        if (onError) {
          onError(error)
        } else {
          alert('PDF生成失败: ' + error.message)
        }
      })
  } catch (error) {
    console.error('生成PDF时发生错误:', error)
    if (onError) {
      onError(error)
    } else {
      alert('PDF生成失败: ' + error.message)
    }
  }
}

/**
 * 根据选择器生成文件名
 */
function generateFilename(selector) {
  const now = new Date()
  const dateStr = now.getFullYear() + 
                  String(now.getMonth() + 1).padStart(2, '0') + 
                  String(now.getDate()).padStart(2, '0')
  const timeStr = String(now.getHours()).padStart(2, '0') + 
                  String(now.getMinutes()).padStart(2, '0')
  
  let prefix = '报表'
  if (selector) {
    if (selector.includes('sales-report')) {
      prefix = '月度销售统计'
    } else if (selector.includes('employee-order-report')) {
      prefix = '员工订单汇总'
    } else if (selector.includes('summary-report')) {
      prefix = '个人订单统计'
    } else if (selector.includes('print-container')) {
      prefix = '配送单'
    }
  }
  
  return `${prefix}_${dateStr}_${timeStr}.pdf`
}

/**
 * 检查数据是否准备好
 * @param {Array|Object} data 要检查的数据
 * @param {string} dataName 数据名称（用于错误提示）
 * @returns {boolean} 数据是否准备好
 */
export function checkDataReady(data, dataName = '数据') {
  if (!data) {
    console.warn(`${dataName}为空`)
    return false
  }
  
  if (Array.isArray(data) && data.length === 0) {
    console.warn(`${dataName}数组为空`)
    return false
  }
  
  return true
}
