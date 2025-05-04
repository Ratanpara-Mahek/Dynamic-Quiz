package np.com.bimalkafle.quizonline

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import np.com.bimalkafle.quizonline.databinding.ActivityQuizHistoryBinding

class QuizHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizHistoryBinding
    private lateinit var quizResultList: MutableList<QuizResult>
    private lateinit var adapter: QuizResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizResultList = mutableListOf()
        adapter = QuizResultAdapter(quizResultList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchQuizResults()
    }

    private fun fetchQuizResults() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(user.uid)
            .child("quizResults")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    quizResultList.clear()
                    for (resultSnapshot in snapshot.children) {
                        val quizResult = resultSnapshot.getValue(QuizResult::class.java)
                        if (quizResult != null) {
                            quizResultList.add(quizResult)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }
}