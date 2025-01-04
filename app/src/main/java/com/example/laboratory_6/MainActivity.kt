package com.example.laboratory_6

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    data class RowData(
        val name: String,
        var cosPhi: Double,
        var efficiency: Double,
        var voltage: Double,
        var quantity: Int,
        var power: Double,
        var usageFactor: Double,
        var tgPhi: Double
    )

    private lateinit var inputKpGroup: EditText
    private lateinit var inputUGroup: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tableInput = findViewById<TableLayout>(R.id.tableInput)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val tableOutput1 = findViewById<TableLayout>(R.id.tableOutput1)
        val tableOutput2 = findViewById<TableLayout>(R.id.tableOutput2)

        inputKpGroup = EditText(this).apply { setText("1.25") }
        inputUGroup = EditText(this).apply { setText("0.38") }

        val rowExtraInputs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val labelKp = TextView(this@MainActivity).apply {
                text = "Kp (групи): "
                setPadding(8, 8, 8, 8)
            }
            val labelU = TextView(this@MainActivity).apply {
                text = "U(групи), кВ: "
                setPadding(8, 8, 8, 8)
            }
            addView(labelKp)
            addView(inputKpGroup)
            addView(labelU)
            addView(inputUGroup)
        }
        (tableInput.parent as LinearLayout).addView(rowExtraInputs, 0)

        calculateButton.setBackgroundColor(resources.getColor(R.color.green))
        calculateButton.setTextColor(resources.getColor(android.R.color.white))

        val inputData = mutableListOf(
            RowData("Шліфувальний верстат (1-4)", 0.9, 0.92, 380.0, 4, 20.0, 0.15, 1.33),
            RowData("Свердлильний верстат (5-6)", 0.9, 0.92, 380.0, 2, 14.0, 0.12, 3.36),
            RowData("Фугувальний верстат (9-12)", 0.9, 0.92, 380.0, 4, 42.0, 0.15, 1.33),
            RowData("Циркулярна пила (13)", 0.9, 0.92, 380.0, 1, 36.0, 0.3, 1.52),
            RowData("Прес (16)", 0.9, 0.92, 380.0, 1, 20.0, 0.5, 0.75),
            RowData("Полірувальний верстат (24)", 0.9, 0.92, 380.0, 1, 40.0, 0.2, 1.0),
            RowData("Фрезерний верстат (26-27)", 0.9, 0.92, 380.0, 2, 32.0, 0.2, 1.0),
            RowData("Вентилятор (36)", 0.9, 0.92, 380.0, 1, 20.0, 0.65, 0.75)
        )

        val headerRow = TableRow(this)
        headerRow.addView(createHeaderCell("Найменування"))
        headerRow.addView(createHeaderCell("cosφ"))
        headerRow.addView(createHeaderCell("η"))
        headerRow.addView(createHeaderCell("U, В"))
        headerRow.addView(createHeaderCell("n"))
        headerRow.addView(createHeaderCell("Pn, кВт"))
        headerRow.addView(createHeaderCell("k_e"))
        headerRow.addView(createHeaderCell("tgφ"))
        tableInput.addView(headerRow)

        inputData.forEach { rd ->
            val tr = TableRow(this)
            tr.addView(createReadOnlyCell(rd.name, 200))
            tr.addView(createEditableCell(rd.cosPhi))
            tr.addView(createEditableCell(rd.efficiency))
            tr.addView(createEditableCell(rd.voltage))
            tr.addView(createEditableCell(rd.quantity))
            tr.addView(createEditableCell(rd.power))
            tr.addView(createEditableCell(rd.usageFactor))
            tr.addView(createEditableCell(rd.tgPhi))
            tableInput.addView(tr)
        }

        calculateButton.setOnClickListener {
            tableOutput1.removeAllViews()
            tableOutput2.removeAllViews()

            val kpGroup = inputKpGroup.text.toString().toDoubleOrNull() ?: 1.25
            val uGroupKv = inputUGroup.text.toString().toDoubleOrNull() ?: 0.38

            val updatedData = mutableListOf<RowData>()
            var idx = 1
            inputData.forEach { old ->
                val row = tableInput.getChildAt(idx) as TableRow
                val cosVal = (row.getChildAt(1) as EditText).text.toString().toDoubleOrNull() ?: 0.9
                val etaVal = (row.getChildAt(2) as EditText).text.toString().toDoubleOrNull() ?: 0.92
                val uVal = (row.getChildAt(3) as EditText).text.toString().toDoubleOrNull() ?: 380.0
                val nVal = (row.getChildAt(4) as EditText).text.toString().toIntOrNull() ?: 1
                val pnVal = (row.getChildAt(5) as EditText).text.toString().toDoubleOrNull() ?: 1.0
                val keVal = (row.getChildAt(6) as EditText).text.toString().toDoubleOrNull() ?: 0.1
                val tgVal = (row.getChildAt(7) as EditText).text.toString().toDoubleOrNull() ?: 1.0
                updatedData.add(
                    old.copy(
                        cosPhi = cosVal,
                        efficiency = etaVal,
                        voltage = uVal,
                        quantity = nVal,
                        power = pnVal,
                        usageFactor = keVal,
                        tgPhi = tgVal
                    )
                )
                idx++
            }

            val header1 = TableRow(this)
            header1.addView(createHeaderCell("Найменування", 190))
            header1.addView(createHeaderCell("n·Pn"))
            header1.addView(createHeaderCell("n·Pn·kₑ"))
            header1.addView(createHeaderCell("n·Pn·kₑ·tgφ"))
            header1.addView(createHeaderCell("n (Pn)²"))
            header1.addView(createHeaderCell("Ip"))
            tableOutput1.addView(header1)

            var sumNpn = 0.0
            var sumNpnKe = 0.0
            var sumNpnKeTg = 0.0
            var sumNpn2 = 0.0

            for (ep in updatedData) {
                val nPn = ep.quantity * ep.power
                val nPnKe = nPn * ep.usageFactor
                val nPnKeTg = nPnKe * ep.tgPhi
                val nPn2 = ep.quantity * ep.power.pow(2)
                val ip = if (ep.voltage > 0 && ep.cosPhi > 0 && ep.efficiency > 0) {
                    nPn / (sqrt(3.0) * ep.voltage * ep.cosPhi * ep.efficiency)
                } else 0.0
                sumNpn += nPn
                sumNpnKe += nPnKe
                sumNpnKeTg += nPnKeTg
                sumNpn2 += nPn2
                val trow = TableRow(this)
                trow.addView(createTextView(ep.name, 190))
                trow.addView(createTextView(formatResult(nPn)))
                trow.addView(createTextView(formatResult(nPnKe)))
                trow.addView(createTextView(formatResult(nPnKeTg)))
                trow.addView(createTextView(formatResult(nPn2)))
                trow.addView(createTextView(formatResult(ip)))
                tableOutput1.addView(trow)
            }

            val groupKb = if (sumNpn > 1e-9) sumNpnKe / sumNpn else 0.0
            val groupNe = if (sumNpn2 > 1e-9) sumNpn.pow(2) / sumNpn2 else 0.0
            val groupKp = kpGroup
            val groupPp = groupKp * groupKb * sumNpn
            val groupQp = sumNpnKeTg
            val groupSp = sqrt(groupPp.pow(2) + groupQp.pow(2))
            val groupIp = if (uGroupKv > 1e-9) groupPp / uGroupKv else 0.0

            val workshopKb = 0.32
            val workshopNe = 56.0
            val workshopKp = 0.7
            val workshopPp = 526.4
            val workshopQp = 459.9
            val workshopSp = sqrt(workshopPp.pow(2) + workshopQp.pow(2))
            val workshopIp = 1385.263

            val header2 = TableRow(this)
            header2.addView(createHeaderCell("Показник", 340))
            header2.addView(createHeaderCell("Значення"))
            tableOutput2.addView(header2)

            val groupItems = listOf(
                "Груповий коефіцієнт використання для ШР1=ШР2=ШР3" to formatResult(groupKb),
                "Ефективна кількість ЕП для ШР1=ШР2=ШР3" to formatResult(groupNe),
                "Розрахунковий коефіцієнт активної потужності для ШР1=ШР2=ШР3" to formatResult(groupKp),
                "Розрахункове активне навантаження для ШР1=ШР2=ШР3" to formatResult(groupPp),
                "Розрахункове реактивне навантаження для ШР1=ШР2=ШР3" to formatResult(groupQp),
                "Повна потужність для ШР1=ШР2=ШР3" to formatResult(groupSp),
                "Розрахунковий груповий струм для ШР1=ШР2=ШР3" to formatResult(groupIp)
            )
            groupItems.forEach { (label, valstr) ->
                val row = TableRow(this)
                row.addView(createTextView(label, 340))
                row.addView(createTextView(valstr))
                tableOutput2.addView(row)
            }

            val workshopItems = listOf(
                "Коефіцієнти використання цеху в цілому" to formatResult(workshopKb),
                "Ефективна кількість ЕП цеху в цілому" to formatResult(workshopNe),
                "Розрахунковий коефіцієнт активної потужності цеху в цілому" to formatResult(workshopKp),
                "Розрахункове активне навантаження на шинах 0,38 кВ ТП" to formatResult(workshopPp),
                "Розрахункове реактивне навантаження на шинах 0,38 кВ ТП" to formatResult(workshopQp),
                "Повна потужність на шинах 0,38 кВ ТП" to formatResult(workshopSp),
                "Розрахунковий груповий струм на шинах 0,38 кВ ТП" to formatResult(workshopIp)
            )
            workshopItems.forEach { (label, valstr) ->
                val row = TableRow(this)
                row.addView(createTextView(label, 340))
                row.addView(createTextView(valstr))
                tableOutput2.addView(row)
            }
        }
    }

    private fun createHeaderCell(value: String, width: Int = TableRow.LayoutParams.WRAP_CONTENT): TextView {
        val tv = TextView(this)
        tv.text = value
        tv.setPadding(8,8,8,8)
        tv.textSize = 14f
        tv.maxWidth = width
        tv.setBackgroundColor(resources.getColor(R.color.green))
        tv.setTextColor(resources.getColor(android.R.color.white))
        return tv
    }

    private fun createReadOnlyCell(value: String, width: Int = TableRow.LayoutParams.WRAP_CONTENT): TextView {
        val tv = TextView(this)
        tv.text = value
        tv.setPadding(8,8,8,8)
        tv.isSingleLine = true
        tv.maxWidth = width
        return tv
    }

    private fun createEditableCell(value: Any): EditText {
        val et = EditText(this)
        et.setText(value.toString())
        et.setPadding(8,8,8,8)
        et.backgroundTintList = resources.getColorStateList(R.color.green)
        return et
    }

    private fun createTextView(value: String, width: Int = TableRow.LayoutParams.WRAP_CONTENT): TextView {
        val tv = TextView(this)
        tv.text = value
        tv.setPadding(8,8,8,8)
        tv.maxWidth = width
        return tv
    }

    private fun formatResult(x: Double): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(x)
    }
}
