package com.example.compoundinterest

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var tvInterest: TextView
    private lateinit var tvDeposit: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvYears: TextView
    private lateinit var barChart: BarChart

    private lateinit var seekDeposit: SeekBar
    private lateinit var seekRate: SeekBar
    private lateinit var seekYears: SeekBar

    // Tooltipy
    private lateinit var tvDepositTooltip: TextView
    private lateinit var tvRateTooltip: TextView
    private lateinit var tvYearsTooltip: TextView

    // predvolené hodnoty
    private var deposit = 10000
    private var rate = 5
    private var years = 5


    private val nf: NumberFormat = NumberFormat.getNumberInstance(Locale("sk","SK"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTotal = findViewById(R.id.tvTotal)
        tvInterest = findViewById(R.id.tvInterest)
        tvDeposit = findViewById(R.id.tvDeposit)
        tvRate = findViewById(R.id.tvRate)
        tvYears = findViewById(R.id.tvYears)
        barChart = findViewById(R.id.barChart)

        seekDeposit = findViewById(R.id.seekDeposit)
        seekRate = findViewById(R.id.seekRate)
        seekYears = findViewById(R.id.seekYears)

        // Tooltipy
        tvDepositTooltip = findViewById(R.id.tvDepositTooltip)
        tvRateTooltip = findViewById(R.id.tvRateTooltip)
        tvYearsTooltip = findViewById(R.id.tvYearsTooltip)

        // inicializácia seekbarov
        seekDeposit.progress = deposit / 10000
        seekRate.progress = rate
        seekYears.progress = years

        // listener pre všetky seekbary
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar?.id) {
                    R.id.seekDeposit -> {
                        deposit = progress * 10000
                        updateTooltipText(tvDepositTooltip, deposit)
                    }
                    R.id.seekRate -> {
                        rate = progress
                        updateTooltipText(tvRateTooltip, rate)
                    }
                    R.id.seekYears -> {
                        years = progress
                        updateTooltipText(tvYearsTooltip, years)
                    }
                }
                updateUI()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                when (seekBar?.id) {
                    R.id.seekDeposit -> {
                        updateTooltipText(tvDepositTooltip, deposit)
                        tvDepositTooltip.visibility = TextView.VISIBLE
                    }
                    R.id.seekRate -> {
                        updateTooltipText(tvRateTooltip, rate)
                        tvRateTooltip.visibility = TextView.VISIBLE
                    }
                    R.id.seekYears -> {
                        updateTooltipText(tvYearsTooltip, years)
                        tvYearsTooltip.visibility = TextView.VISIBLE
                    }
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                when (seekBar?.id) {
                    R.id.seekDeposit -> tvDepositTooltip.visibility = TextView.INVISIBLE
                    R.id.seekRate -> tvRateTooltip.visibility = TextView.INVISIBLE
                    R.id.seekYears -> tvYearsTooltip.visibility = TextView.INVISIBLE
                }
            }
        }

        seekDeposit.setOnSeekBarChangeListener(listener)
        seekRate.setOnSeekBarChangeListener(listener)
        seekYears.setOnSeekBarChangeListener(listener)

        setupChart()
        updateUI()
    }

    private fun updateTooltipText(tooltip: TextView, value: Int) {
        // Aktualizujeme text tooltipu
        tooltip.text = nf.format(value)

        // Po nastavení textu počkáme na prekreslenie a potom pozicujeme
        tooltip.post {
            updateTooltipPosition(tooltip)
        }
    }

    private fun updateTooltipPosition(tooltip: TextView) {
        val seekBar = when (tooltip.id) {
            R.id.tvDepositTooltip -> seekDeposit
            R.id.tvRateTooltip -> seekRate
            R.id.tvYearsTooltip -> seekYears
            else -> return
        }

        val progress = seekBar.progress
        val max = seekBar.max

        // Vypočítame pozíciu palca - konvertujeme na Float
        val thumbPos = (seekBar.width - seekBar.paddingLeft - seekBar.paddingRight) *
                progress.toFloat() / max.toFloat() + seekBar.paddingLeft

        // Posunieme tooltip nad palec - konvertujeme na Float
        val tooltipWidth = tooltip.width.toFloat()
        val xPosition = thumbPos - (tooltipWidth / 2)

        // Obmedzenie, aby tooltip nevyšiel z okraja
        val maxX = (seekBar.width - tooltip.width).toFloat()
        val finalX = xPosition.coerceIn(0f, maxX)

        tooltip.translationX = finalX
        tooltip.translationY = (-seekBar.height - tooltip.height).toFloat()
    }

    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            0 -> "Vklad"
                            1 -> "Úroky"
                            else -> ""
                        }
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
        }
    }

    private fun updateUI() {
        // vzorec: A = P * (1 + r/100)^t
        val totalDouble = deposit * Math.pow(1 + rate / 100.0, years.toDouble())
        val total = Math.round(totalDouble).toLong()
        val interest = total - deposit

        // texty (s formátovaním)
        tvTotal.text = "Nasporená suma: ${nf.format(total)}"
        tvInterest.text = "Z toho úroky: ${nf.format(interest)}"

        tvDeposit.text = "Vklad: ${nf.format(deposit)}"
        tvRate.text = "Úrok: $rate %"
        tvYears.text = "Obdobie: $years rokov"

        // grafické dáta
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, deposit.toFloat()))
        entries.add(BarEntry(1f, interest.toFloat()))

        val set = BarDataSet(entries, "Vklad/Úroky")
        // hodnoty vo vnútri pruhov formátované
        set.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return nf.format(value.toLong())
            }
        }
        set.valueTextSize = 12f
        set.colors = listOf(Color.parseColor("#CFFCF0"), Color.parseColor("#7FD3C8"))

        val data = BarData(set)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.invalidate()
        barChart.animateY(400)
    }
}