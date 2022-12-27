package com.example.lab_1

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import kotlin.properties.Delegates
import kotlin.random.Random


class ListGenerator(
    leftBorderInterval: Int,
    rightBorderInterval: Int,
    amountElements: Int
) {
    private val listRandNum = mutableListOf<Int>()
    private val normalizedList = mutableListOf<Double>()
    init {
        for (i in 1..amountElements) {
            listRandNum.add(Random(System.nanoTime()).nextInt(rightBorderInterval - leftBorderInterval + 1) + leftBorderInterval)
        }
        val minimumValue = listRandNum.min()
        val maximumValue = listRandNum.max()
        for (i in 1..amountElements) {
            normalizedList.add((listRandNum[i - 1].toDouble() - minimumValue!!) / (maximumValue!! - minimumValue))
        }
    }
    fun retListRandNum(): MutableList<Int> {
        return listRandNum
    }
    fun retNormalizedList(): MutableList<Double> {
        return normalizedList
    }
}

class ListNumbersAdapter(
    private val normalizedList: MutableList<Double>,
    private val listRandNum: MutableList<Int>
) : RecyclerView.Adapter<ListNumbersAdapter.ListNumbersViewHolder>() {

    class ListNumbersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val largeTextView: TextView = itemView.findViewById(R.id.textViewLarge)
        val smallTextView: TextView = itemView.findViewById(R.id.textViewSmall)
    }

    fun clearItems() {
        normalizedList.clear()
        listRandNum.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNumbersViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_item,
            parent,
            false
        )
        return ListNumbersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListNumbersViewHolder, position: Int) {
        val decimalFormat = DecimalFormat("#.###")
        holder.largeTextView.text = decimalFormat.format(normalizedList[position])
        holder.smallTextView.text = ("Исходный элемент: " + listRandNum[position].toString())
    }

    override fun getItemCount() = normalizedList.size
}

class AboutAppDialogScreen : DialogFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.dialog_window, container, false)
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.window_round)
        val okButton = view.findViewById(R.id.ImageButtonOk) as ImageButton
        okButton.setOnClickListener(this)
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.ImageButtonOk -> {
                dialog!!.cancel()
            }
        }
    }
}

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "MyActivity"
    var leftBorderInterval: Int? = null
    var rightBorderInterval: Int? = null
    var numberOfListItems: Int? = null
    private var listOfNumber: ListGenerator by Delegates.notNull()
    private lateinit var listNumberRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImageButtonConfirm.visibility = View.INVISIBLE
        val toolBar: Toolbar = findViewById(R.id.AppHeader)
        setSupportActionBar(toolBar)
        title = "Нормализатор"
        toolBar.subtitle = "Данных"
        toolBar.setLogo(R.mipmap.ic_launcher)
        val maxLengthInputField = 10
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(maxLengthInputField)
        InputFieldIntervalBoundaries1.filters = filterArray
        InputFieldIntervalBoundaries2.filters = filterArray
        InputFieldNumberElements.filters = filterArray
        listNumberRecyclerView = findViewById(R.id.ListGeneratedData)
        listNumberRecyclerView.layoutManager = LinearLayoutManager(this)
        fun Long.toIntThrowing() : Int {
            if (this < Int.MIN_VALUE || this > Int.MAX_VALUE) {
                throw Exception("Value is out of range of data type Int!")
                //throw RuntimeException()
            }
            return this.toInt()
        }
        InputFieldIntervalBoundaries1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                whenAllNotNull(leftBorderInterval, rightBorderInterval, numberOfListItems)
                if (!s.isNullOrEmpty()) {
                    if (s.matches("^0[\\d]+".toRegex())) {
                        InputFieldIntervalBoundaries1.setText("0")
                        InputFieldIntervalBoundaries1.setSelection(InputFieldIntervalBoundaries1.length())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputCorrect1 = (InputFieldIntervalBoundaries1.background as GradientDrawable).mutate()
                val inputCorrect2 = (InputFieldIntervalBoundaries2.background as GradientDrawable).mutate()
                val valueBoundaries1: Int?
                val valueBoundaries2: Int?
                if (!s.isNullOrEmpty()) {
                    if (TextUtils.isDigitsOnly(s)) {
                        valueBoundaries1 = try { s.toString().toLong().toIntThrowing() } catch (e: Exception) { null }
                        if (valueBoundaries1 != null) {
                            if (!TextUtils.isEmpty(InputFieldIntervalBoundaries2.text) &&
                                TextUtils.isDigitsOnly(InputFieldIntervalBoundaries2.text)) {
                                valueBoundaries2 = try { InputFieldIntervalBoundaries2.text.toString().toLong().toIntThrowing() }
                                catch (e: Exception) { null }
                                if (valueBoundaries2 != null) {
                                    if (valueBoundaries1 < valueBoundaries2) {
                                        leftBorderInterval = valueBoundaries1
                                        rightBorderInterval = valueBoundaries2
                                        (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                        (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                    } else {
                                        leftBorderInterval = null
                                        (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                                    }
                                } else {
                                    leftBorderInterval = valueBoundaries1
                                    (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                }
                            } else {
                                leftBorderInterval = valueBoundaries1
                                (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                            }
                        } else {
                            leftBorderInterval = null
                            (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                            valueBoundaries2 = try { InputFieldIntervalBoundaries2.text.toString().toLong().toIntThrowing() }
                            catch (e: Exception) { null }
                            if (valueBoundaries2 != null) {
                                rightBorderInterval = valueBoundaries2
                                (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                            }
                        }
                    } else {
                        leftBorderInterval = null
                        (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                        valueBoundaries2 = try { InputFieldIntervalBoundaries2.text.toString().toLong().toIntThrowing() }
                        catch (e: Exception) { null }
                        if (valueBoundaries2 != null) {
                            rightBorderInterval = valueBoundaries2
                            (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                        }
                    }
                } else {
                    leftBorderInterval = null
                    (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                    valueBoundaries2 = try { InputFieldIntervalBoundaries2.text.toString().toLong().toIntThrowing() }
                    catch (e: Exception) { null }
                    if (valueBoundaries2 != null) {
                        rightBorderInterval = valueBoundaries2
                        (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                    }
                }
            }
        })
        InputFieldIntervalBoundaries2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                whenAllNotNull(leftBorderInterval, rightBorderInterval, numberOfListItems)
                if (!s.isNullOrEmpty()) {
                    if (s.matches("^0".toRegex())) {
                        InputFieldIntervalBoundaries2.setText("")
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputCorrect2 = (InputFieldIntervalBoundaries2.background as GradientDrawable).mutate()
                val inputCorrect1 = (InputFieldIntervalBoundaries1.background as GradientDrawable).mutate()
                val valueBoundaries1: Int?
                val valueBoundaries2: Int?
                if (!s.isNullOrEmpty()) {
                    if (TextUtils.isDigitsOnly(s)) {
                        valueBoundaries2 = try { s.toString().toLong().toIntThrowing() }
                        catch (e: Exception) { null }
                        if (valueBoundaries2 != null) {
                            if (!TextUtils.isEmpty(InputFieldIntervalBoundaries1.text) &&
                                TextUtils.isDigitsOnly(InputFieldIntervalBoundaries1.text)) {
                                valueBoundaries1 = try { InputFieldIntervalBoundaries1.text.toString().toLong().toIntThrowing() }
                                catch (e: Exception) { null }
                                if (valueBoundaries1 != null) {
                                    if (valueBoundaries1 < valueBoundaries2) {
                                        rightBorderInterval = valueBoundaries2
                                        leftBorderInterval = valueBoundaries1
                                        (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                        (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                    } else {
                                        rightBorderInterval = null
                                        (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                                    }
                                } else {
                                    rightBorderInterval = valueBoundaries2
                                    (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                                }
                            } else {
                                rightBorderInterval = valueBoundaries2
                                (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                            }
                        } else {
                            rightBorderInterval = null
                            (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                            valueBoundaries1 = try { InputFieldIntervalBoundaries1.text.toString().toLong().toIntThrowing() }
                            catch (e: Exception) { null }
                            if (valueBoundaries1 != null) {
                                leftBorderInterval = valueBoundaries1
                                (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                            }
                        }
                    } else {
                        rightBorderInterval = null
                        (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                        valueBoundaries1 = try { InputFieldIntervalBoundaries1.text.toString().toLong().toIntThrowing() }
                        catch (e: Exception) { null }
                        if (valueBoundaries1 != null) {
                            leftBorderInterval = valueBoundaries1
                            (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                        }
                    }
                } else {
                    rightBorderInterval = null
                    (inputCorrect2 as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                    valueBoundaries1 = try { InputFieldIntervalBoundaries1.text.toString().toLong().toIntThrowing() }
                    catch (e: Exception) { null }
                    if (valueBoundaries1 != null) {
                        leftBorderInterval = valueBoundaries1
                        (inputCorrect1 as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                    }
                }
            }
        })
        InputFieldNumberElements.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                whenAllNotNull(leftBorderInterval, rightBorderInterval, numberOfListItems)
                if (!s.isNullOrEmpty()) {
                    if (s.matches("^[01]".toRegex())) {
                        InputFieldNumberElements.setText("")
                        InputFieldNumberElements.setSelection(InputFieldNumberElements.length())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputCorrect = (InputFieldNumberElements.background as GradientDrawable).mutate()
                if (!s.isNullOrEmpty()) {
                    if (TextUtils.isDigitsOnly(s)) {
                        val valueNumberElement = try { s.toString().toLong().toIntThrowing() }
                        catch (e: Exception) { null }
                        if (valueNumberElement != null) {
                            if (valueNumberElement > 1) {
                                numberOfListItems = valueNumberElement
                                (inputCorrect as GradientDrawable).setColor(Color.parseColor("#B2FF59"))
                            } else {
                                numberOfListItems = null
                                (inputCorrect as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                            }
                        } else {
                            numberOfListItems = null
                            (inputCorrect as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                        }
                    } else {
                        numberOfListItems = null
                        (inputCorrect as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                    }
                } else {
                    numberOfListItems = null
                    (inputCorrect as GradientDrawable).setColor(Color.parseColor("#FF6E40"))
                }
            }
        })
        ImageButtonClear1.setOnClickListener(this)
        ImageButtonClear2.setOnClickListener(this)
        ImageButtonClear3.setOnClickListener(this)
        ImageButtonConfirm.setOnClickListener(this)
        ImageButtonClearAll.setOnClickListener(this)
    }
    private fun <T : Any> whenAllNotNull(vararg options: T?) {
        if (options.all { it != null }) {
            ImageButtonConfirm.visibility = View.VISIBLE
        }
        else {
            ImageButtonConfirm.visibility = View.INVISIBLE
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ImageButtonClearAll -> {
                InputFieldIntervalBoundaries1.text.clear()
                InputFieldIntervalBoundaries2.text.clear()
                InputFieldNumberElements.text.clear()
            }
            R.id.ImageButtonClear1 -> {
                InputFieldIntervalBoundaries1.text.clear()
            }
            R.id.ImageButtonClear2 -> {
                InputFieldIntervalBoundaries2.text.clear()
            }
            R.id.ImageButtonClear3 -> {
                InputFieldNumberElements.text.clear()
            }
            R.id.ImageButtonConfirm -> {
                listOfNumber = ListGenerator(
                    leftBorderInterval!!,
                    rightBorderInterval!!,
                    numberOfListItems!!
                )
                val adapter = ListNumbersAdapter(listOfNumber.retNormalizedList(), listOfNumber.retListRandNum())
                listNumberRecyclerView.adapter = adapter
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId === R.id.about_app) {
            val myDialogFragment = AboutAppDialogScreen()
            val manager = supportFragmentManager
            myDialogFragment.show(manager, "myDialog")
        }
        return true
    }
}