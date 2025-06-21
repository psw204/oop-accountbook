package com.example.project1

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project1.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        if (username == null) {
            Toast.makeText(context, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show()
            // Optional: Redirect to login screen
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Users/$username/transactions")

        setupPieChart()
        fetchAndProcessTransactions()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "수입/지출"
            setCenterTextSize(16f)
            legend.isEnabled = true
        }
    }

    private fun fetchAndProcessTransactions() {
        valueEventListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0.0
                var totalExpense = 0.0

                for (transactionSnapshot in snapshot.children) {
                    val transaction = transactionSnapshot.getValue(TransactionModel::class.java)
                    if (transaction != null) {
                        if (transaction.type == "income") {
                            totalIncome += transaction.amount ?: 0.0
                        } else {
                            totalExpense += transaction.amount ?: 0.0
                        }
                    }
                }

                updateUI(totalIncome, totalExpense)
                updatePieChart(totalIncome, totalExpense)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "데이터 로딩 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(income: Double, expense: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale.KOREA)
        binding.totalIncomeTextView.text = format.format(income)
        binding.totalExpenseTextView.text = format.format(expense)
        val balance = income - expense
        binding.balanceTextView.text = format.format(balance)

        if (balance < 0) {
            binding.balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        } else {
            binding.balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
        }
    }

    private fun updatePieChart(income: Double, expense: Double) {
        if (income == 0.0 && expense == 0.0) {
            binding.pieChart.visibility = View.GONE
            return
        }
        binding.pieChart.visibility = View.VISIBLE

        val entries = ArrayList<PieEntry>()
        if (income > 0) entries.add(PieEntry(income.toFloat(), "수입"))
        if (expense > 0) entries.add(PieEntry(expense.toFloat(), "지출"))

        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
        )

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter())
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.WHITE)

        binding.pieChart.data = pieData
        binding.pieChart.invalidate() // refresh
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::database.isInitialized && ::valueEventListener.isInitialized) {
            database.removeEventListener(valueEventListener)
        }
        _binding = null
    }
}