package com.example.project1

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project1.databinding.FragmentTransactionsBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var transactionsAdapter: TransactionAdapter
    private val transactionsList = mutableListOf<TransactionModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
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

        setupRecyclerView()

        binding.saveButton.setOnClickListener {
            saveTransaction()
        }

        fetchTransactions()
    }

    private fun setupRecyclerView() {
        transactionsAdapter = TransactionAdapter(transactionsList)
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = transactionsAdapter
        }
    }

    private fun saveTransaction() {
        val description = binding.descriptionEditText.text.toString().trim()
        val amountString = binding.amountEditText.text.toString().trim()

        if (description.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(context, "내용과 금액을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(context, "유효한 금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (binding.expenseRadioButton.isChecked) "expense" else "income"
        val timestamp = System.currentTimeMillis()
        val transactionId = database.push().key

        if (transactionId == null) {
            Toast.makeText(context, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = TransactionModel(transactionId, description, amount, type, timestamp)

        database.child(transactionId).setValue(transaction)
            .addOnSuccessListener {
                Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                binding.descriptionEditText.text?.clear()
                binding.amountEditText.text?.clear()
                binding.expenseRadioButton.isChecked = true
            }
            .addOnFailureListener {
                Toast.makeText(context, "저장에 실패했습니다: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTransactions() {
        valueEventListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionsList.clear()
                for (transactionSnapshot in snapshot.children) {
                    val transaction = transactionSnapshot.getValue(TransactionModel::class.java)
                    if (transaction != null) {
                        transactionsList.add(transaction)
                    }
                }
                transactionsAdapter.notifyDataSetChanged()
                if (transactionsList.isNotEmpty()) {
                    binding.transactionsRecyclerView.scrollToPosition(transactionsList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "데이터를 불러오는데 실패했습니다: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::database.isInitialized && ::valueEventListener.isInitialized) {
            database.removeEventListener(valueEventListener)
        }
        _binding = null
    }
}

class TransactionAdapter(private val transactions: List<TransactionModel>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        private val amountTextView: TextView = itemView.findViewById(R.id.amount_text_view)
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)

        fun bind(transaction: TransactionModel) {
            descriptionTextView.text = transaction.description
            val formattedAmount = String.format("%,.0f원", transaction.amount ?: 0.0)
            amountTextView.text = if (transaction.type == "expense") "-$formattedAmount" else "+$formattedAmount"
            amountTextView.setTextColor(
                if (transaction.type == "expense") itemView.context.getColor(android.R.color.holo_red_dark)
                else itemView.context.getColor(android.R.color.holo_blue_dark)
            )

            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            dateTextView.text = sdf.format(Date(transaction.timestamp ?: 0))
        }
    }
}