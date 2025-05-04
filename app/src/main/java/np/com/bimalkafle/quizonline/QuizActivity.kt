package np.com.bimalkafle.quizonline

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import np.com.bimalkafle.quizonline.databinding.ActivityQuizBinding
import np.com.bimalkafle.quizonline.databinding.ScoreDialogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var questionModelList: List<QuestionModel> = listOf()
        var time: String = ""
        var startTime: Long = 0L
        var endTime: Long = 0L
    }

    private lateinit var binding: ActivityQuizBinding
    private var currentQuestionIndex = 0
    private var selectedAnswer = ""
    private var score = 0
    private var quizId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }

        if (questionModelList.isEmpty()) {
            Toast.makeText(this, "No questions available for this quiz", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        startTime = intent.getLongExtra("START_TIME", 0L)
        endTime = intent.getLongExtra("END_TIME", 0L)

        if (quizId.isEmpty()) {
            Toast.makeText(this, "Quiz ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < startTime) {
            Toast.makeText(this, "This quiz is not yet available", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (currentTime > endTime) {
            Toast.makeText(this, "This quiz is no longer available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadQuestions()
        startTimer()
    }

    private fun startTimer() {
        val totalTimeInMillis = if (time.isNotEmpty()) time.toInt() * 60 * 1000L else 0L
        if (totalTimeInMillis <= 0) {
            Toast.makeText(this, "Invalid time for quiz", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        object : CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                finishQuiz()
            }
        }.start()
    }

    private fun loadQuestions() {
        selectedAnswer = ""
        if (currentQuestionIndex >= questionModelList.size) {
            finishQuiz()
            return
        }

        val currentQuestion = questionModelList[currentQuestionIndex]
        binding.apply {
            questionIndicatorTextview.text = "Question ${currentQuestionIndex + 1}/ ${questionModelList.size}"
            questionProgressIndicator.progress =
                ((currentQuestionIndex.toFloat() / questionModelList.size.toFloat()) * 100).toInt()
            questionTextview.text = currentQuestion.question

            val options = currentQuestion.options.toMutableList()
            while (options.size < 4) {
                options.add("")
            }
            btn0.text = options[0]
            btn1.text = options[1]
            btn2.text = options[2]
            btn3.text = options[3]
        }
    }

    override fun onClick(view: View?) {
        binding.apply {
            btn0.setBackgroundColor(getColor(R.color.gray))
            btn1.setBackgroundColor(getColor(R.color.gray))
            btn2.setBackgroundColor(getColor(R.color.gray))
            btn3.setBackgroundColor(getColor(R.color.gray))
        }

        val clickedBtn = view as Button
        if (clickedBtn.id == R.id.next_btn) {
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(applicationContext, "Please select answer to continue", Toast.LENGTH_SHORT).show()
                return
            }
            if (selectedAnswer == questionModelList[currentQuestionIndex].correct) {
                score++
                Log.i("Score of quiz", score.toString())
            }
            currentQuestionIndex++
            loadQuestions()
        } else {
            selectedAnswer = clickedBtn.text.toString()
            clickedBtn.setBackgroundColor(getColor(R.color.orange))
        }
    }

    private fun finishQuiz() {
        val totalQuestions = questionModelList.size
        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            scoreProgressIndicator.progress = percentage
            scoreProgressText.text = "$percentage %"
            if (percentage > 50) {
                scoreTitle.text = "Congrats! You have passed"
                scoreTitle.setTextColor(Color.BLUE)
            } else {
                scoreTitle.text = "Oops! You have failed"
                scoreTitle.setTextColor(Color.RED)
            }
            scoreSubtitle.text = "$score out of $totalQuestions are correct"

            finishBtn.setOnClickListener {
                finish()
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val resultId = FirebaseDatabase.getInstance().reference.push().key ?: return
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val quizResult = QuizResult(
                quizId = quizId,
                score = score,
                totalQuestions = totalQuestions,
                percentage = percentage,
                timestamp = timestamp
            )

            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(user.uid)
                .child("quizResults")
                .child(resultId)
                .setValue(quizResult)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quiz result saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save quiz result", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }
}