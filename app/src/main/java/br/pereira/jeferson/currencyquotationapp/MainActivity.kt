package br.pereira.jeferson.currencyquotationapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import br.pereira.jeferson.currencyquotationapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fromCurrency: String
    private lateinit var toCurrency: String
    var currenciesList: LinkedHashMap<*, *> = linkedMapOf<String, String>()
    var currenciesSymbolsArray: ArrayList<String> = arrayListOf()
    var currencyQuotationHashMap: LinkedHashMap<*, *> = linkedMapOf<String, String>()
    private lateinit var currencyQuotationCode: String
    private lateinit var currencyQuotationValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = "Currency Quotation App"
        }

        getCurrenciesList {
            setSpinnersCurrenciesList()
        }

        getCurrencyQuotation {
            setCurrencyQuotationTextInfo()
        }

        fromCurrency = binding.textSearchableSpinnerFromCurrency.text.toString()
        toCurrency = binding.textSearchableSpinnerToCurrency.text.toString()
    }

    private fun getCurrenciesList(onResponseCallback: OnResponseCallback? = null) {
        val call = Network.create()

        call.getCurrencyList().enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    currenciesList = gson.fromJson(response.body()?.string().toString(), LinkedHashMap::class.java)
                    currenciesList.forEach {
                        currenciesSymbolsArray.add(it.key.toString().uppercase())
                    }
                    onResponseCallback?.execute()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResponseCallback?.execute()
            }
        })
    }

    private fun setSpinnersCurrenciesList() {
        binding.textSearchableSpinnerFromCurrency.apply {
            setOnClickListener {
                configureCurrencyDialog(this)
            }
        }

        binding.textSearchableSpinnerToCurrency.apply {
            setOnClickListener {
                configureCurrencyDialog(this)
            }
        }
    }

    private fun configureCurrencyDialog(textView: TextView) {
        val dialog = createCurrencyDialog()

        val editText: EditText = dialog.findViewById(R.id.edit_text)
        val listView: ListView = dialog.findViewById(R.id.list_view)

        val adapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, currenciesSymbolsArray)

        listView.adapter = adapter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                textView.text = adapter.getItem(position)

                dialog.dismiss()
            }
    }

    private fun createCurrencyDialog(): Dialog {
        val dialog = Dialog(this@MainActivity)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.90).toInt()

        dialog.apply {
            setContentView(R.layout.dialog_searchable_spinner)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(width, height)
            show()
        }
        return dialog
    }

    private fun getCurrencyQuotation(onResponseCallback: OnResponseCallback? = null) {
        binding.buttonSearchCurrencyQuotation.setOnClickListener {
            fromCurrency = binding.textSearchableSpinnerFromCurrency.text.toString()
            toCurrency = binding.textSearchableSpinnerToCurrency.text.toString()
            if (fromCurrency.isNotBlank() && toCurrency.isNotBlank()) {
                val call = Network.create()

                call.getCurrencyQuotation(
                    fromCurrency = fromCurrency.lowercase(),
                    toCurrency = toCurrency.lowercase(),
                ).enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val gson = Gson()
                            currencyQuotationHashMap = gson.fromJson(response.body()?.string().toString(), LinkedHashMap::class.java)
                            currencyQuotationHashMap.forEach {
                                if (it.key.toString().equals(toCurrency, ignoreCase = true)) {
                                    currencyQuotationValue = it.value.toString()
                                    currencyQuotationCode = it.key.toString()
                                } else {
                                    currencyQuotationValue = ""
                                    currencyQuotationCode = ""
                                }
                            }
                            onResponseCallback?.execute()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        onResponseCallback?.execute() }
                })
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please, fill all fields before search quotation!",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun setCurrencyQuotationTextInfo() {
        var quotationValue = currencyQuotationValue
        val nf = NumberFormat.getInstance(Locale("BR"))
        quotationValue = nf.format(quotationValue.toDoubleOrNull() ?: 0)

        val stringMessage = "Today<br>" +
            "<span style='color:#007E00'>1</span> $fromCurrency<br>" +
            "=<br>" +
            "<span style='color:#007E00'>$quotationValue</span> ${currencyQuotationCode.uppercase()}"

        binding.textCurrencyQuoteResult.text =
            HtmlCompat.fromHtml(stringMessage, 0)
    }
}
