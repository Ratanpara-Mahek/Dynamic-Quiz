package np.com.bimalkafle.quizonline

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import np.com.bimalkafle.quizonline.databinding.ActivityAddQuizBinding
import java.text.SimpleDateFormat
import java.util.*

class EditQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddQuizBinding
    private val questionLayouts = mutableListOf<LinearLayout>()
    private val questionEdits = mutableListOf<EditText>()
    private val option1Edits = mutableListOf<EditText>()
    private val option2Edits = mutableListOf<EditText>()
    private val option3Edits = mutableListOf<EditText>()
    private val option4Edits = mutableListOf<EditText>()
    private val correctAnswerEdits = mutableListOf<EditText>()
    private var submitButton: Button? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private var startCalendar: Calendar = Calendar.getInstance()
    private var endCalendar: Calendar = Calendar.getInstance()
    private lateinit var quizId: String
    private var questionList: ArrayList<QuestionModel> = ArrayList()
    private val TAG = "EditQuizActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Safely retrieve intent extras
        quizId = intent.getStringExtra("QUIZ_ID") ?: run {
            Log.e(TAG, "QUIZ_ID is null")
            Toast.makeText(this, "Invalid quiz data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.quizTitleEdit.setText(intent.getStringExtra("QUIZ_TITLE") ?: "")
        binding.quizSubtitleEdit.setText(intent.getStringExtra("QUIZ_SUBTITLE") ?: "")
        binding.quizTimeEdit.setText(intent.getStringExtra("QUIZ_TIME") ?: "")
        val questionListExtra = intent.getSerializableExtra("QUESTION_LIST")
        if (questionListExtra is ArrayList<*>) {
            @Suppress("UNCHECKED_CAST")
            questionList = questionListExtra as ArrayList<QuestionModel>
        } else {
            Log.e(TAG, "QUESTION_LIST is null or invalid")
            Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.numQuestionsEdit.setText(questionList.size.toString())

        val startTime = intent.getLongExtra("START_TIME", 0L)
        val endTime = intent.getLongExtra("END_TIME", 0L)
        if (startTime > 0 && endTime > 0) {
            startCalendar.timeInMillis = startTime
            endCalendar.timeInMillis = endTime
            binding.startTimeEdit.setText(dateFormat.format(startCalendar.time))
            binding.endTimeEdit.setText(dateFormat.format(endCalendar.time))
        } else {
            Log.e(TAG, "Invalid startTime or endTime")
            Toast.makeText(this, "Invalid time data", Toast.LENGTH_SHORT).show()
        }


        binding.startTimeEdit.setOnClickListener { showDateTimePicker(true) }
        binding.endTimeEdit.setOnClickListener { showDateTimePicker(false) }

        binding.addQuestionBtn.setOnClickListener {
            addQuestions()
        }


        addQuestions(true)
    }

    private fun showDateTimePicker(isStart: Boolean) {
        val calendar = if (isStart) startCalendar else endCalendar
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val selectedDateTime = dateFormat.format(calendar.time)
                        if (isStart) {
                            binding.startTimeEdit.setText(selectedDateTime)
                        } else {
                            binding.endTimeEdit.setText(selectedDateTime)
                        }
                    },
                    currentHour,
                    currentMinute,
                    true
                )
                timePickerDialog.show()
            },
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.show()
    }

    private fun addQuestions(preload: Boolean = false) {
        val numQuestionsStr = binding.numQuestionsEdit.text.toString().trim()
        if (numQuestionsStr.isEmpty()) {
            Toast.makeText(this, "Please enter the number of questions", Toast.LENGTH_SHORT).show()
            return
        }

        val numQuestions = numQuestionsStr.toIntOrNull() ?: 0
        if (numQuestions <= 0) {
            Toast.makeText(this, "Please enter a valid number of questions", Toast.LENGTH_SHORT).show()
            return
        }


        questionLayouts.clear()
        questionEdits.clear()
        option1Edits.clear()
        option2Edits.clear()
        option3Edits.clear()
        option4Edits.clear()
        correctAnswerEdits.clear()
        submitButton?.let { binding.container.removeView(it) }
        binding.container.removeViews(6, binding.container.childCount - 6)


        for (i in 1..numQuestions) {
            val questionLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 16 }
            }

            val questionEdit = EditText(this).apply {
                hint = "Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            questionLayout.addView(questionEdit)
            questionEdits.add(questionEdit)

            val option1Edit = EditText(this).apply {
                hint = "Option 1 for Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            }
            questionLayout.addView(option1Edit)
            option1Edits.add(option1Edit)

            val option2Edit = EditText(this).apply {
                hint = "Option 2 for Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            }
            questionLayout.addView(option2Edit)
            option2Edits.add(option2Edit)

            val option3Edit = EditText(this).apply {
                hint = "Option 3 for Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            }
            questionLayout.addView(option3Edit)
            option3Edits.add(option3Edit)

            val option4Edit = EditText(this).apply {
                hint = "Option 4 for Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            }
            questionLayout.addView(option4Edit)
            option4Edits.add(option4Edit)

            val correctAnswerEdit = EditText(this).apply {
                hint = "Correct Answer for Question $i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            }
            questionLayout.addView(correctAnswerEdit)
            correctAnswerEdits.add(correctAnswerEdit)

            if (preload && i <= questionList.size) {
                val question = questionList[i - 1]
                questionEdit.setText(question.question)
                if (question.options.size >= 4) {
                    option1Edit.setText(question.options[0])
                    option2Edit.setText(question.options[1])
                    option3Edit.setText(question.options[2])
                    option4Edit.setText(question.options[3])
                }
                correctAnswerEdit.setText(question.correct)
            }

            binding.container.addView(questionLayout)
            questionLayouts.add(questionLayout)
        }

        submitButton = Button(this).apply {
            text = "Update Quiz"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
            setOnClickListener { updateQuiz() }
        }
        binding.container.addView(submitButton)
    }

    private fun updateQuiz() {
        val title = binding.quizTitleEdit.text.toString().trim()
        val subtitle = binding.quizSubtitleEdit.text.toString().trim()
        val time = binding.quizTimeEdit.text.toString().trim()
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis
        val numQuestionsStr = binding.numQuestionsEdit.text.toString().trim()

        if (title.isEmpty() || subtitle.isEmpty() || time.isEmpty() || numQuestionsStr.isEmpty()) {
            Toast.makeText(this, "Please fill all basic fields", Toast.LENGTH_SHORT).show()
            return
        }

        val numQuestions = numQuestionsStr.toIntOrNull() ?: 0
        if (numQuestions <= 0 || questionEdits.size != numQuestions) {
            Toast.makeText(this, "Please add questions first", Toast.LENGTH_SHORT).show()
            return
        }

        if (endTime <= startTime) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        val questionModels = mutableListOf<QuestionModel>()
        for (i in 0 until numQuestions) {
            val question = questionEdits[i].text.toString().trim()
            val options = listOf(
                option1Edits[i].text.toString().trim(),
                option2Edits[i].text.toString().trim(),
                option3Edits[i].text.toString().trim(),
                option4Edits[i].text.toString().trim()
            )
            val correct = correctAnswerEdits[i].text.toString().trim()

            if (question.isEmpty() || options.any { it.isEmpty() } || correct.isEmpty() || !options.contains(correct)) {
                Toast.makeText(this, "Please fill all fields for Question ${i + 1} and ensure correct answer is in options", Toast.LENGTH_SHORT).show()
                return
            }

            questionModels.add(QuestionModel(question, options, correct))
        }

        val quizModel = QuizModel(quizId, title, subtitle, time, questionModels, startTime, endTime)
        Log.d(TAG, "Updating quiz with ID: $quizId, Title: $title")

        FirebaseDatabase.getInstance().reference.child("quizzes").child(quizId)
            .setValue(quizModel)
            .addOnSuccessListener {
                Log.d(TAG, "Quiz updated successfully")
                Toast.makeText(this, "Quiz updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update quiz: ${exception.message}")
                Toast.makeText(this, "Failed to update quiz", Toast.LENGTH_SHORT).show()
            }
    }
}