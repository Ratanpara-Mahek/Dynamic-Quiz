package np.com.bimalkafle.quizonline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import np.com.bimalkafle.quizonline.databinding.ActivityTeacherMainBinding

class TeacherMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherMainBinding
    private lateinit var quizModelList: MutableList<QuizModel>
    private lateinit var adapter: QuizListAdapter
    private val TAG = "TeacherMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizModelList = mutableListOf()
        getDataFromFirebase()

        binding.addQuizBtn.setOnClickListener {
            startActivity(Intent(this, AddQuizActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.progressBar.visibility = View.GONE
        adapter = QuizListAdapter(
            quizModelList,
            onDeleteClick = { quiz ->
                Log.d(TAG, "Deleting quiz with ID: ${quiz.id}")
                FirebaseDatabase.getInstance().reference.child("quizzes").child(quiz.id)
                    .removeValue()
                    .addOnSuccessListener {
                        quizModelList.remove(quiz)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Quiz deleted", Toast.LENGTH_SHORT).show()
                    }
            },
            onEditClick = { quiz ->
                Log.d(TAG, "Editing quiz with ID: ${quiz.id}")
                val intent = Intent(this, EditQuizActivity::class.java).apply {
                    putExtra("QUIZ_ID", quiz.id)
                    putExtra("QUIZ_TITLE", quiz.title)
                    putExtra("QUIZ_SUBTITLE", quiz.subtitle)
                    putExtra("QUIZ_TIME", quiz.time)
                    putExtra("START_TIME", quiz.startTime)
                    putExtra("END_TIME", quiz.endTime)
                    // Convert List to ArrayList for serialization
                    val questionArrayList = ArrayList(quiz.questionList)
                    putExtra("QUESTION_LIST", questionArrayList)
                }
                startActivity(intent)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference.child("quizzes")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    quizModelList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val quizModel = snapshot.getValue(QuizModel::class.java)
                        if (quizModel != null) {
                            quizModelList.add(quizModel)
                        } else {
                            Log.e(TAG, "Failed to parse quiz data from snapshot: ${snapshot.key}")
                        }
                    }
                }
                setupRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch quizzes: ${exception.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}