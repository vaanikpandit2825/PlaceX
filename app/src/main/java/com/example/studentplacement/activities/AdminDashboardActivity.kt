package com.example.studentplacement.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONObject

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    // Charts
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var barChart: HorizontalBarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        session = SessionManager(this)

        // ── Text views ───────────────────────────────────────────────────────
        val tvWelcome         = findViewById<TextView>(R.id.tvAdminWelcome)
        val tvStudents        = findViewById<TextView>(R.id.tvAdminTotalStudents)
        val tvPlaced          = findViewById<TextView>(R.id.tvAdminPlaced)
        val tvPct             = findViewById<TextView>(R.id.tvAdminPlacedPct)
        val tvJobs            = findViewById<TextView>(R.id.tvAdminJobs)
        val tvApps            = findViewById<TextView>(R.id.tvAdminApps)
        val tvPending         = findViewById<TextView>(R.id.tvAdminPending)
        val tvAvgPkg          = findViewById<TextView>(R.id.tvAdminAvgPkg)
        val tvPiePlaced       = findViewById<TextView>(R.id.tvPieChartPlacedPct)
        val tvPieUnplaced     = findViewById<TextView>(R.id.tvPieChartUnplacedPct)

        // ── Dynamic containers ───────────────────────────────────────────────
        val llPendingCos      = findViewById<LinearLayout>(R.id.llPendingCompanies)
        val llRecentApps      = findViewById<LinearLayout>(R.id.llRecentApps)
        val llBranchPlacement = findViewById<LinearLayout>(R.id.llBranchPlacement)
        val llTopStudents     = findViewById<LinearLayout>(R.id.llTopStudents)



        // ── Charts ───────────────────────────────────────────────────────────
        lineChart = findViewById(R.id.chartApplicationsOverTime)
        pieChart  = findViewById(R.id.chartPlacementRate)
        barChart  = findViewById(R.id.chartTopCompanies)

        tvWelcome.text = "Welcome, ${session.getStudentName()}"

        // Setup chart shells immediately (no data yet, just styling)
        setupLineChart()
        setupPieChart()
        setupBarChart()

        // ── API call ─────────────────────────────────────────────────────────
        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "admin_dashboard.php",
            { response ->
                try {
                    val obj = JSONObject(response)

                    // ── Stats ────────────────────────────────────────────────
                    val placedCount  = obj.getString("total_placed").toIntOrNull() ?: 0
                    val totalCount   = obj.getString("total_students").toIntOrNull() ?: 0
                    val placedPct    = obj.getString("placement_pct").toFloatOrNull() ?: 0f

                    tvStudents.text  = obj.getString("total_students")
                    tvPlaced.text    = obj.getString("total_placed")
                    tvPct.text       = "${obj.getString("placement_pct")}%"
                    tvJobs.text      = obj.getString("total_jobs")
                    tvApps.text      = obj.getString("total_apps")
                    tvPending.text   = obj.getString("pending_cos")
                    tvAvgPkg.text    = "₹ ${obj.getString("avg_package")} LPA"

                    // ── Chart 2: Donut – Placement Rate ──────────────────────
                    val unplacedPct = (100f - placedPct).coerceAtLeast(0f)
                    tvPiePlaced.text   = "${placedPct.toInt()}%"
                    tvPieUnplaced.text = "${unplacedPct.toInt()}%"
                    loadPieChart(placedPct, unplacedPct)

                    // ── Chart 3: Horizontal Bar – Branch/Company hiring ──────
                    // Uses branch_placement data: shows placed count per branch
                    val branchArr = obj.optJSONArray("branch_placement")

                    val barLabels = mutableListOf<String>()
                    val barValues = mutableListOf<BarEntry>()

                    if (branchArr != null && branchArr.length() > 0) {

                        for (i in 0 until branchArr.length()) {
                            val b = branchArr.getJSONObject(i)

                            val label = b.optString("branch", "B${i+1}")
                            val value = b.optString("placed", "0").toFloatOrNull() ?: 0f

                            barLabels.add(label)
                            barValues.add(BarEntry(i.toFloat(), value))
                        }

                        loadBarChart(barLabels, barValues)

                    } else {
                        barChart.clear()
                    }
                    // ── Chart 1: Line – Applications over time ───────────────
                    // If your API returns a "monthly_apps" array use it.
                    // Otherwise we build a trend from recent_apps count per status as fallback.
                    val monthlyArr = obj.optJSONArray("monthly_apps")

                    if (monthlyArr != null && monthlyArr.length() > 0) {

                        val lineLabels = mutableListOf<String>()
                        val lineValues = mutableListOf<Entry>()

                        for (i in 0 until monthlyArr.length()) {
                            val m = monthlyArr.getJSONObject(i)

                            val label = m.optString("month", "M${i+1}")
                            val value = m.optString("count", "0").toFloatOrNull() ?: 0f

                            lineLabels.add(label)
                            lineValues.add(Entry(i.toFloat(), value))
                        }

                        loadLineChart(lineLabels, lineValues)

                    } else {
                        lineChart.clear()
                    }

                    // ── Pending company approvals ────────────────────────────
                    llPendingCos.removeAllViews()
                    val pending = obj.getJSONArray("pending_companies")
                    if (pending.length() == 0) {
                        llPendingCos.addView(makeText("No pending approvals", "#607090"))
                    }
                    for (i in 0 until pending.length()) {
                        val c = pending.getJSONObject(i)
                        val card = layoutInflater.inflate(R.layout.item_pending_company, llPendingCos, false)
                        card.findViewById<TextView>(R.id.tvPendingCoName).text      = c.getString("company_name")
                        card.findViewById<TextView>(R.id.tvPendingCoIndustry).text  = "${c.getString("industry")} • ${c.getString("location")}"
                        card.findViewById<TextView>(R.id.tvPendingCoRecruiter).text = "Recruiter: ${c.optString("recruiter_name","N/A")} (${c.optString("recruiter_email","")})"
                        card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApprove).setOnClickListener {
                            approveCompany(c.getInt("company_id"), "approve", llPendingCos)
                        }
                        card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReject).setOnClickListener {
                            approveCompany(c.getInt("company_id"), "reject", llPendingCos)
                        }
                        llPendingCos.addView(card)
                    }

                    // ── Branch placement breakdown (text rows) ───────────────
                    llBranchPlacement.removeAllViews()
                    for (i in 0 until branchArr.length()) {
                        val b = branchArr.getJSONObject(i)
                        val row = makeBranchRow(
                            branch  = b.getString("branch"),
                            placed  = b.getString("placed").toIntOrNull() ?: 0,
                            total   = b.getString("total").toIntOrNull() ?: 0,
                            pct     = b.getString("pct").toFloatOrNull() ?: 0f
                        )
                        llBranchPlacement.addView(row)
                    }

                    // ── Top students ─────────────────────────────────────────
                    llTopStudents.removeAllViews()
                    val topArr = obj.getJSONArray("top_students")
                    for (i in 0 until topArr.length()) {
                        val s = topArr.getJSONObject(i)
                        llTopStudents.addView(makeStudentRow(
                            rank   = i + 1,
                            name   = s.getString("name"),
                            branch = s.getString("branch"),
                            cgpa   = s.getString("cgpa"),
                            apps   = s.getString("apps_count")
                        ))
                        if (i < topArr.length() - 1) llTopStudents.addView(makeDivider())
                    }

                    // ── Recent applications ───────────────────────────────────
                    llRecentApps.removeAllViews()
                    val recentArr = obj.getJSONArray("recent_apps")
                    for (i in 0 until recentArr.length()) {
                        val a = recentArr.getJSONObject(i)
                        llRecentApps.addView(makeAppRow(a))
                        if (i < recentArr.length() - 1) llRecentApps.addView(makeDivider())
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Parse error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { Toast.makeText(this, "Network error — is XAMPP running?", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams() = mapOf("admin_id" to session.getAdminId().toString())
        }
        Volley.newRequestQueue(this).add(req)

        // ── Button listeners ─────────────────────────────────────────────────

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //   CHART SETUP — styling only, no data yet
    // ════════════════════════════════════════════════════════════════════

    private fun setupLineChart() {
        lineChart.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled    = false
            legend.isEnabled         = false
            setTouchEnabled(true)
            isDragEnabled            = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            axisRight.isEnabled      = false

            xAxis.apply {
                position        = XAxis.XAxisPosition.BOTTOM
                textColor       = Color.parseColor("#607090")
                textSize        = 10f
                gridColor       = Color.parseColor("#1A607090")
                axisLineColor   = Color.parseColor("#1A607090")
                setDrawAxisLine(false)
                granularity     = 1f
            }
            axisLeft.apply {
                textColor       = Color.parseColor("#607090")
                textSize        = 10f
                gridColor       = Color.parseColor("#1A607090")
                axisLineColor   = Color.parseColor("#1A607090")
                setDrawAxisLine(false)
            }
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled    = false
            legend.isEnabled         = false
            isDrawHoleEnabled        = true
            holeRadius               = 62f
            transparentCircleRadius  = 66f
            setHoleColor(Color.parseColor("#111C30"))
            setTransparentCircleColor(Color.parseColor("#111C30"))
            setTransparentCircleAlpha(80)
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(18f)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            setTouchEnabled(false)
        }
    }

    private fun setupBarChart() {
        barChart.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled    = false
            legend.isEnabled         = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawValueAboveBar(true)
            xAxis.apply {
                textColor = Color.parseColor("#B0BEC5")
                textSize = 11f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
            }

            axisLeft.apply {
                textColor = Color.parseColor("#607090")
                textSize = 10f
                gridColor = Color.parseColor("#1A607090")
                setDrawAxisLine(false)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            xAxis.apply {
                textColor            = Color.parseColor("#B0BEC5")
                textSize             = 11f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity          = 1f
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //   CHART DATA LOADERS
    // ════════════════════════════════════════════════════════════════════

    private fun loadLineChart(labels: List<String>, entries: List<Entry>) {
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.axisMaximum =
            (entries.maxOfOrNull { it.y } ?: 10f) * 1.2f
        val dataSet = LineDataSet(entries, "Applications").apply {
            color                   = Color.parseColor("#4A90E2")
            valueTextColor          = Color.TRANSPARENT   // hide value labels on points
            lineWidth               = 2.5f
            circleRadius            = 4f
            setCircleColor(Color.parseColor("#4A90E2"))
            circleHoleColor         = Color.parseColor("#111C30")
            circleHoleRadius        = 2f
            mode                    = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor               = Color.parseColor("#4A90E2")
            fillAlpha               = 30
            setDrawHighlightIndicators(false)
        }

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.xAxis.labelCount     = labels.size
        lineChart.data                 = LineData(dataSet)
        lineChart.animateX(900, Easing.EaseInOutQuart)
        lineChart.invalidate()
    }

    private fun loadPieChart(placed: Float, unplaced: Float) {
        val entries = listOf(
            PieEntry(placed,   "Placed"),
            PieEntry(unplaced, "Not Placed")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors          = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#1A4A90E2")
            )
            sliceSpace      = 2f
            selectionShift  = 0f
            setDrawValues(false)
        }

        pieChart.setCenterText("${placed.toInt()}%")
        pieChart.data = PieData(dataSet)
        pieChart.animateY(900, Easing.EaseInOutQuart)
        pieChart.invalidate()
    }

    private fun loadBarChart(labels: List<String>, entries: List<BarEntry>) {
        val barColors = listOf(
            Color.parseColor("#4A90E2"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#E040FB"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#FF5722")
        )

        val dataSet = BarDataSet(entries, "").apply {
            colors     = (entries.indices).map { barColors[it % barColors.size] }
            valueTextColor  = Color.parseColor("#B0BEC5")
            valueTextSize   = 10f
            valueFormatter  = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = value.toInt().toString()
            }
        }

        val barData = BarData(dataSet).apply { barWidth = 0.5f }

        barChart.xAxis.apply {
            valueFormatter  = IndexAxisValueFormatter(labels)
            labelCount      = labels.size
        }
        barChart.data = barData
        barChart.animateY(900, Easing.EaseInOutQuart)
        barChart.invalidate()
    }

    // ════════════════════════════════════════════════════════════════════
    //   HELPER VIEWS
    // ════════════════════════════════════════════════════════════════════

    /** Branch row with a mini progress bar */
    private fun makeBranchRow(branch: String, placed: Int, total: Int, pct: Float): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }

        // Label row
        val labelRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        labelRow.addView(TextView(this).apply {
            text      = branch
            textSize  = 12f
            setTextColor(Color.parseColor("#FFFFFF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        labelRow.addView(TextView(this).apply {
            text      = "$placed / $total  (${pct.toInt()}%)"
            textSize  = 11f
            setTextColor(Color.parseColor("#607090"))
        })
        row.addView(labelRow)

        // Progress bar
        val progress = android.widget.ProgressBar(
            this, null, android.R.attr.progressBarStyleHorizontal
        ).apply {
            max           = 100
            setProgress(pct.toInt(), false)
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 6
            ).also { it.topMargin = 6 }
            progressDrawable = android.graphics.drawable.ClipDrawable(
                android.graphics.drawable.ColorDrawable(Color.parseColor("#4A90E2")),
                android.view.Gravity.START,
                android.graphics.drawable.ClipDrawable.HORIZONTAL
            )
            background    = android.graphics.drawable.ColorDrawable(Color.parseColor("#1A607090"))
        }
        row.addView(progress)
        return row
    }

    /** Student rank row */
    private fun makeStudentRow(rank: Int, name: String, branch: String, cgpa: String, apps: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }

        // Rank badge
        row.addView(TextView(this).apply {
            text      = "#$rank"
            textSize  = 11f
            setTextColor(Color.parseColor("#4A90E2"))
            layoutParams = LinearLayout.LayoutParams(32.dp, LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        // Name + branch
        val info = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        info.addView(TextView(this).apply {
            text      = name
            textSize  = 13f
            setTextColor(Color.WHITE)
            typeface  = android.graphics.Typeface.DEFAULT_BOLD
        })
        info.addView(TextView(this).apply {
            text      = "$branch • CGPA $cgpa"
            textSize  = 11f
            setTextColor(Color.parseColor("#607090"))
        })
        row.addView(info)

        // Apps count pill
        row.addView(TextView(this).apply {
            text              = "$apps apps"
            textSize          = 10f
            setTextColor(Color.parseColor("#00BCD4"))
            setPadding(10, 4, 10, 4)
            background        = roundedBackground("#0D00BCD4", 20f)
        })

        return row
    }

    /** Recent application row */
    private fun makeAppRow(a: JSONObject): LinearLayout {
        val status = a.getString("status")
        val statusColor = when (status) {
            "Selected"   -> "#4CAF50"
            "Rejected"   -> "#F44336"
            "Shortlisted"-> "#2196F3"
            else         -> "#FF9800"
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }

        // Student + company info
        val info = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        info.addView(TextView(this).apply {
            text      = "${a.getString("student_name")} → ${a.getString("job_title")}"
            textSize  = 13f
            setTextColor(Color.WHITE)
            typeface  = android.graphics.Typeface.DEFAULT_BOLD
        })
        info.addView(TextView(this).apply {
            text      = "${a.getString("company_name")} • ${a.getString("branch")} • CGPA ${a.getString("cgpa")}"
            textSize  = 11f
            setTextColor(Color.parseColor("#607090"))
        })
        row.addView(info)

        // Status pill
        row.addView(TextView(this).apply {
            text              = status
            textSize          = 10f
            setTextColor(Color.parseColor(statusColor))
            setPadding(10, 4, 10, 4)
            background        = roundedBackground("${statusColor}22", 20f)
        })

        return row
    }

    private fun makeDivider() = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).also { it.topMargin = 2; it.bottomMargin = 2 }
        setBackgroundColor(Color.parseColor("#1A607090"))
    }

    private fun makeText(text: String, color: String = "#FFFFFF") = TextView(this).apply {
        this.text = text
        setTextColor(Color.parseColor(color))
        textSize = 13f
        setPadding(0, 8, 0, 8)
    }

    private fun roundedBackground(hexColor: String, radius: Float): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape         = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(Color.parseColor(hexColor))
            cornerRadius  = radius
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    // ════════════════════════════════════════════════════════════════════
    //   NETWORK
    // ════════════════════════════════════════════════════════════════════

    private fun approveCompany(companyId: Int, action: String, container: LinearLayout) {
        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "admin_approve_company.php",
            {
                val msg = if (action == "approve") "Company approved!" else "Company rejected"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                recreate()
            }, {}
        ) {
            override fun getParams() = mapOf(
                "company_id" to companyId.toString(),
                "action"     to action
            )
        }
        Volley.newRequestQueue(this).add(req)
    }
}