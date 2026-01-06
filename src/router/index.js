import { createRouter, createWebHistory } from 'vue-router'

// 基础页面
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'

// 员工模块
import EmployeeMenu from '../views/employee/Menu.vue'
import EmployeeNewOrder from '../views/employee/NewOrder.vue'
import EmployeeOrderList from '../views/employee/OrderList.vue'
import EmployeeOrderDetail from '../views/employee/OrderDetail.vue'
import EmployeeOrderConfirmation from '../views/employee/OrderConfirmation.vue'
import EmployeePersonalOrderSummary from '../views/employee/PersonalOrderSummary.vue'

// 配送模块
import DeliveryDashboard from '../views/delivery/Dashboard.vue'
import DeliveryOrderDetail from '../views/delivery/OrderDetail.vue'
import DeliveryPrintTemplate from '../views/delivery/PrintTemplate.vue'

// 厨房模块
import KitchenDashboard from '../views/kitchen/Dashboard.vue'
import KitchenBlanketOrder from '../views/kitchen/BlanketOrder.vue'

// 财务模块
import FinanceDashboard from '../views/finance/Dashboard.vue'
import FinanceEmployeeOrderSummary from '../views/finance/EmployeeOrderSummary.vue'
import FinanceMenuDetail from '../views/finance/MenuDetail.vue'
import FinanceMonthlySalesReport from '../views/finance/MonthlySalesReport.vue'

// 管理员模块
import ManagerDashboard from '../views/manager/Dashboard.vue'
import ManagerMenuManagement from '../views/manager/MenuManagement.vue'
import ManagerMenuForm from '../views/manager/MenuForm.vue'
import ManagerMenuDetail from '../views/manager/MenuDetail.vue'
import ManagerRecipeManagement from '../views/manager/RecipeManagement.vue'
import ManagerBlanketOrder from '../views/manager/BlanketOrder.vue'
import ManagerEmployeeOrderSummary from '../views/manager/EmployeeOrderSummary.vue'
import ManagerSalesReport from '../views/manager/SalesReport.vue'
import ManagerSystemConfig from '../views/manager/SystemConfig.vue'
import ManagerUserManagement from '../views/manager/UserManagement.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/index', component: Home },
  { path: '/login', component: Login },

  // 员工
  { path: '/employee/menu', component: EmployeeMenu },
  { path: '/employee/new-order', component: EmployeeNewOrder },
  { path: '/employee/order-list', component: EmployeeOrderList },
  { path: '/employee/order-detail/:id', component: EmployeeOrderDetail, props: true },
  { path: '/employee/order-confirmation/:id', component: EmployeeOrderConfirmation, props: true },
  { path: '/employee/personal-order-summary', component: EmployeePersonalOrderSummary },

  // 配送
  { path: '/delivery/dashboard', component: DeliveryDashboard },
  { path: '/delivery/order-detail/:id', component: DeliveryOrderDetail, props: true },
  { path: '/delivery/print-template/:id?', component: DeliveryPrintTemplate, props: true },

  // 厨房
  { path: '/kitchen/dashboard', component: KitchenDashboard },
  { path: '/kitchen/blanket-order', component: KitchenBlanketOrder },

  // 财务
  { path: '/finance/dashboard', component: FinanceDashboard },
  { path: '/finance/employee-order-summary', component: FinanceEmployeeOrderSummary },
  { path: '/finance/menu-detail/:id', component: FinanceMenuDetail, props: true },
  { path: '/finance/monthly-sales-report', component: FinanceMonthlySalesReport },

  // 管理员
  { path: '/manager/dashboard', component: ManagerDashboard },
  { path: '/manager/menu-management', component: ManagerMenuManagement },
  { path: '/manager/menu-form/:id?', component: ManagerMenuForm, props: true },
  { path: '/manager/menu-detail/:id', component: ManagerMenuDetail, props: true },
  { path: '/manager/recipe-management', component: ManagerRecipeManagement },
  { path: '/manager/blanket-order', component: ManagerBlanketOrder },
  { path: '/manager/employee-order-summary', component: ManagerEmployeeOrderSummary },
  { path: '/manager/sales-report', component: ManagerSalesReport },
  { path: '/manager/system-config', component: ManagerSystemConfig },
  { path: '/manager/user-management', component: ManagerUserManagement }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router


