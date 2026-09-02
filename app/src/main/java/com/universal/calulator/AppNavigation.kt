package com.universal.calulator

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object AppRoutes {
    const val CALC_HOME = "calc_home"
    const val TOOLS_HUB = "tools_hub"
    const val ABOUT_SCREEN = "about_screen"
    const val CATEGORY_SCREEN = "category/{categoryId}"
    const val TOOL_SCREEN = "tool/{toolId}"

    fun categoryRoute(categoryId: String): String = "category/$categoryId"
    fun toolRoute(toolId: String): String = "tool/$toolId"
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.CALC_HOME
    ) {
        // 1. Calculator Screen
        composable(AppRoutes.CALC_HOME) {
            CalculatorScreen(
                onOpenTools = { navController.navigate(AppRoutes.TOOLS_HUB) },
                onOpenAbout = { navController.navigate(AppRoutes.ABOUT_SCREEN) }
            )
        }

        // 2. About Toolator Screen
        composable(AppRoutes.ABOUT_SCREEN) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Tools Hub Screen
        composable(AppRoutes.TOOLS_HUB) {
            ToolsHubScreen(
                onBack = { navController.popBackStack() },
                onCategoryClick = { catId -> navController.navigate(AppRoutes.categoryRoute(catId)) },
                onToolClick = { toolId -> navController.navigate(AppRoutes.toolRoute(toolId)) }
            )
        }

        // 4. Category Screen
        composable(
            route = AppRoutes.CATEGORY_SCREEN,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStack ->
            val catId = backStack.arguments?.getString("categoryId")
            CategoryScreen(
                categoryId = catId,
                onBack = { navController.popBackStack() },
                onToolClick = { toolId -> navController.navigate(AppRoutes.toolRoute(toolId)) }
            )
        }

        // 5. Dynamic Screen Router
        composable(
            route = AppRoutes.TOOL_SCREEN,
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStack ->
            val toolId = backStack.arguments?.getString("toolId")?.lowercase() ?: "length"

            val loanCompareTools = listOf("emi", "loan_planner", "loan_compare", "tool_emi", "tool_loan")
            val investmentTools = listOf("inv", "sip", "investment", "tool_investment")
            val otherFinanceTools = listOf("simple_interest", "compound_interest", "si", "ci", "tool_si", "tool_ci")
            val taxTools = listOf("gst", "disc", "discount", "tool_gst", "tool_discount")
            val dateTools = listOf("age", "datediff", "date_diff", "tool_age", "tool_datediff", "tool_date_diff")
            val healthTools = listOf("bmi", "health", "calorie", "tool_bmi", "tool_health", "bmi_body", "act_burn")

            val numCheckTools = listOf("num_check", "tool_num_check")
            val lcmHcfTools = listOf("lcm_hcf", "tool_lcm_hcf")
            val eqSolveTools = listOf("eq_solve", "tool_eq_solve", "equation")
            val baseCalcTools = listOf("num_base", "tool_num_base", "base_calc")
            val ratioTools = listOf("ratio", "tool_ratio")
            val matrixTools = listOf("matrix", "tool_matrix")

            when {
                toolId == "currency" || toolId == "curr" || toolId == "tool_currency" -> {
                    CurrencyConverterScreen(onBack = { navController.popBackStack() })
                }
                toolId in loanCompareTools -> {
                    LoanCompareScreen(onBack = { navController.popBackStack() })
                }
                toolId in investmentTools -> {
                    InvestmentCompareScreen(onBack = { navController.popBackStack() })
                }
                toolId in otherFinanceTools -> {
                    FinanceCalculatorsScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                toolId in taxTools -> {
                    TaxShoppingCalculator(toolId = toolId, onBack = { navController.popBackStack() })
                }
                toolId in dateTools -> {
                    DateTimeCalculatorScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                toolId in healthTools -> {
                    HealthCalculatorScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                toolId == "habit_tracker" || toolId == "tool_habit_tracker" -> {
                    HabitTrackerScreen(onBack = { navController.popBackStack() })
                }

                // Modular Math Tools Routing
                toolId in numCheckTools -> {
                    NumberCheckerScreen(onBack = { navController.popBackStack() })
                }
                toolId in lcmHcfTools -> {
                    LcmHcfScreen(onBack = { navController.popBackStack() })
                }
                toolId in eqSolveTools -> {
                    EquationSolverScreen(onBack = { navController.popBackStack() })
                }
                toolId in baseCalcTools -> {
                    BaseCalculatorScreen(onBack = { navController.popBackStack() })
                }
                toolId in ratioTools -> {
                    RatioProportionScreen(onBack = { navController.popBackStack() })
                }
                toolId in matrixTools -> {
                    MatrixCalculatorScreen(onBack = { navController.popBackStack() })
                }

                else -> {
                    UnitConverterScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}