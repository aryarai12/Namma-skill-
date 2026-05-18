package in.nammaskill.aryakotlin

import android.app.Activity
import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

data class Course(
    val id: Int,
    val title: String,
    val trade: String,
    val duration: String,
    val center: String,
    val eligibility: String,
    val seats: Int,
    val stipend: String,
    val placement: String
)

class MainActivity : Activity() {
    private val green = Color.rgb(22, 122, 87)
    private val dark = Color.rgb(16, 37, 29)
    private val bg = Color.rgb(246, 248, 245)
    private val line = Color.rgb(219, 228, 221)
    private val muted = Color.rgb(93, 108, 100)
    private val gold = Color.rgb(240, 184, 79)

    private val courses = listOf(
        Course(1, "Assistant Electrician", "Electrician", "Long term", "Ramanagara Skill Center", "10th Pass", 24, "Rs. 1,500/month", "Placement support available"),
        Course(2, "Tailoring and Sewing Machine Operator", "Sewing", "Short term", "Magadi Rural Training Hub", "8th Pass", 30, "Toolkit support", "Self-employment support"),
        Course(3, "Web Coding Basics", "Coding", "Short term", "Channapatna Technical Institute", "12th Pass", 18, "Certificate support", "Internship guidance"),
        Course(4, "Mobile Phone Repair", "Mobile Repair", "Short term", "Kanakapura Livelihood Center", "10th Pass", 22, "Rs. 1,000/month", "Shop setup guidance"),
        Course(5, "Welding Technician", "Welding", "Long term", "Ramanagara Skill Center", "10th Pass", 32, "Rs. 1,800/month", "Industry placement support")
    )

    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout
    private lateinit var filterList: LinearLayout
    private lateinit var courseList: LinearLayout
    private lateinit var savedList: LinearLayout
    private lateinit var selectedCourseText: TextView
    private lateinit var summaryOutput: TextView
    private lateinit var saveStatus: TextView
    private lateinit var searchInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var villageInput: EditText
    private lateinit var educationSpinner: Spinner
    private lateinit var motivationInput: EditText

    private var activeFilter = "All"
    private var selectedCourse = courses.first()
    private var latestSummary = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("namma_skill_kotlin", MODE_PRIVATE)

        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(bg)
        root = verticalLayout()
        root.setPadding(dp(18), dp(18), dp(18), dp(30))
        scrollView.addView(root)
        setContentView(scrollView)

        buildHeader()
        buildHero()
        buildCourses()
        buildCenters()
        buildStories()
        buildApplicationForm()
        buildSavedApplications()
        renderCourses()
        renderSavedApplications()
    }

    private fun buildHeader() {
        root.addView(eyebrow("Skill development tracker"))
        root.addView(title("Namma-Skill Kotlin", 30))
        root.addView(body("Native Android app for finding government skill courses, applying, and generating candidate summaries."))
    }

    private fun buildHero() {
        val card = card()
        card.setBackgroundColor(dark)
        card.addView(eyebrow("Self-employment and job-ready training", gold))
        card.addView(title("Find nearby skill courses before the batch starts.", 28, Color.WHITE))
        card.addView(body("Discover courses, check eligibility, request alerts, and generate a candidate summary for training centers.", Color.rgb(230, 239, 233)))

        val metrics = horizontalLayout()
        metrics.addView(metric("5", "Trades"))
        metrics.addView(metric("4", "Centers"))
        metrics.addView(metric("126", "Open seats"))
        card.addView(metrics)
        root.addView(card)
    }

    private fun buildCourses() {
        root.addView(sectionTitle("Course finder", "Available Training Batches"))

        searchInput = EditText(this)
        searchInput.hint = "Search trade or center"
        searchInput.singleLine = true
        searchInput.imeOptions = EditorInfo.IME_ACTION_SEARCH
        styleInput(searchInput)
        searchInput.setOnEditorActionListener { _, _, _ ->
            renderCourses()
            false
        }
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) renderCourses()
        }
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderCourses()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        root.addView(searchInput)

        filterList = wrapLayout()
        root.addView(filterList)
        renderFilters()

        courseList = verticalLayout()
        root.addView(courseList)
    }

    private fun renderFilters() {
        filterList.removeAllViews()
        val filters = listOf("All", "Electrician", "Sewing", "Coding", "Mobile Repair", "Welding", "Short term", "Long term")
        filters.forEach { filter ->
            val button = chip(filter, filter == activeFilter)
            button.setOnClickListener {
                activeFilter = filter
                renderFilters()
                renderCourses()
            }
            filterList.addView(button)
        }
    }

    private fun renderCourses() {
        courseList.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()
        val filtered = courses.filter { course ->
            val matchesFilter = activeFilter == "All" || course.trade == activeFilter || course.duration == activeFilter
            val haystack = "${course.title} ${course.trade} ${course.center}".lowercase()
            matchesFilter && haystack.contains(query)
        }

        if (filtered.isEmpty()) {
            courseList.addView(body("No courses found for this search."))
            return
        }

        filtered.forEach { course ->
            val item = card()
            item.addView(eyebrow("${course.trade} | ${course.duration}"))
            item.addView(title(course.title, 21))
            item.addView(body("Center: ${course.center}\nEligibility: ${course.eligibility}\nSeats: ${course.seats}\nStipend: ${course.stipend}\n${course.placement}"))

            val actions = horizontalLayout()
            val alert = smallButton("Alert me", false)
            alert.setOnClickListener {
                alert.text = "Alert active: ${course.trade}"
                alert.isEnabled = false
                toast("Alert saved for ${course.trade}")
            }
            val apply = smallButton(if (selectedCourse.id == course.id) "Selected" else "Apply", true)
            apply.setOnClickListener {
                selectedCourse = course
                updateSelectedCourseLabel()
                summaryOutput.text = "Selected Course: ${course.title}\n\nFill the candidate form and submit to generate the summary."
                renderCourses()
                toast("${course.title} selected")
            }
            actions.addView(alert)
            actions.addView(apply)
            item.addView(actions)
            courseList.addView(item)
        }
    }

    private fun buildCenters() {
        root.addView(sectionTitle("Nearby centers", "District Skill Centers"))
        val centers = listOf(
            "Ramanagara Skill Center - 2.4 km",
            "Magadi Rural Training Hub - 8.1 km",
            "Kanakapura Livelihood Center - 14 km",
            "Channapatna Technical Institute - 18 km"
        )
        centers.forEach { root.addView(bodyCard(it)) }
    }

    private fun buildStories() {
        root.addView(sectionTitle("Success stories", "Training to Opportunity"))
        root.addView(bodyCard("Asha, Sewing\nCompleted tailoring training and started home-based blouse stitching."))
        root.addView(bodyCard("Ravi, Mobile Repair\nOpened a phone service desk after completing a short-term course."))
        root.addView(bodyCard("Meena, Coding\nBuilt portfolio projects and joined a local digital service center."))
    }

    private fun buildApplicationForm() {
        root.addView(sectionTitle("Candidate profile", "Apply for a Course"))

        selectedCourseText = bodyCard("")
        root.addView(selectedCourseText)
        updateSelectedCourseLabel()

        nameInput = editText("Name", "Arya Kumar")
        phoneInput = editText("Phone", "9876543210")
        phoneInput.inputType = android.text.InputType.TYPE_CLASS_PHONE
        villageInput = editText("Village", "Bidadi")
        educationSpinner = Spinner(this)
        educationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("10th Pass", "12th Pass", "Diploma", "Graduate"))
        motivationInput = editText("Motivation", "I want to learn a job-ready skill and become financially independent.", multiLine = true)

        root.addView(nameInput)
        root.addView(phoneInput)
        root.addView(villageInput)
        root.addView(educationSpinner)
        root.addView(motivationInput)

        val submit = primaryButton("Generate candidate summary")
        submit.setOnClickListener { submitApplication() }
        root.addView(submit)

        root.addView(sectionTitle("Training center summary", "Candidate Summary"))
        summaryOutput = bodyCard("Select a course and submit the form to generate the candidate summary.")
        root.addView(summaryOutput)

        val actions = horizontalLayout()
        val copy = smallButton("Copy summary", false)
        copy.setOnClickListener { copySummary() }
        val clear = smallButton("Clear saved", false)
        clear.setOnClickListener {
            prefs.edit().remove("applications").apply()
            saveStatus.text = "Saved applications cleared."
            renderSavedApplications()
        }
        actions.addView(copy)
        actions.addView(clear)
        root.addView(actions)

        saveStatus = body("No application submitted yet.")
        root.addView(saveStatus)
    }

    private fun submitApplication() {
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val village = villageInput.text.toString().trim()
        val education = educationSpinner.selectedItem.toString()
        val motivation = motivationInput.text.toString().trim()

        if (name.isEmpty() || phone.length != 10 || village.isEmpty() || motivation.isEmpty()) {
            toast("Please enter name, 10 digit phone, village, and motivation.")
            return
        }

        val summary = """
            Candidate Name: $name
            Phone Number: $phone
            Village: $village
            Education: $education

            Preferred Course: ${selectedCourse.title}
            Trade and Duration: ${selectedCourse.trade} - ${selectedCourse.duration}
            Training Center: ${selectedCourse.center}
            Eligibility Match: Candidate education can be verified against ${selectedCourse.eligibility}
            Seats Available: ${selectedCourse.seats}
            Support: ${selectedCourse.stipend}; ${selectedCourse.placement}

            Motivation:
            $motivation

            Requested Action:
            Trainer should call the candidate, confirm documents, and guide them for the next batch registration.
        """.trimIndent()

        latestSummary = summary
        summaryOutput.text = summary

        val applications = JSONArray(prefs.getString("applications", "[]") ?: "[]")
        val saved = JSONObject()
            .put("savedAt", System.currentTimeMillis())
            .put("name", name)
            .put("phone", phone)
            .put("village", village)
            .put("education", education)
            .put("courseTitle", selectedCourse.title)
            .put("center", selectedCourse.center)
            .put("summary", summary)

        val next = JSONArray()
        next.put(saved)
        for (index in 0 until minOf(applications.length(), 7)) {
            next.put(applications.getJSONObject(index))
        }
        prefs.edit().putString("applications", next.toString()).apply()

        saveStatus.text = "Application saved on this device."
        renderSavedApplications()
        toast("Candidate summary generated")
    }

    private fun buildSavedApplications() {
        root.addView(sectionTitle("Saved locally", "Submitted Applications"))
        savedList = verticalLayout()
        root.addView(savedList)
    }

    private fun renderSavedApplications() {
        savedList.removeAllViews()
        val applications = JSONArray(prefs.getString("applications", "[]") ?: "[]")
        if (applications.length() == 0) {
            savedList.addView(body("No saved applications yet. Submit the form to store one here."))
            return
        }

        for (index in 0 until applications.length()) {
            val item = applications.getJSONObject(index)
            val view = bodyCard("${item.getString("name")} - ${item.getString("courseTitle")}\n${item.getString("village")} | ${item.getString("center")}")
            view.setOnClickListener {
                latestSummary = item.getString("summary")
                summaryOutput.text = latestSummary
                saveStatus.text = "Showing saved application summary."
            }
            savedList.addView(view)
        }
    }

    private fun copySummary() {
        val text = latestSummary.ifBlank { summaryOutput.text.toString() }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Candidate Summary", text))
        saveStatus.text = "Summary copied to clipboard."
        toast("Summary copied")
    }

    private fun updateSelectedCourseLabel() {
        selectedCourseText.text = "Selected course: ${selectedCourse.title}"
    }

    private fun sectionTitle(kicker: String, heading: String): LinearLayout {
        val layout = verticalLayout()
        layout.setPadding(0, dp(24), 0, dp(8))
        layout.addView(eyebrow(kicker))
        layout.addView(title(heading, 24))
        return layout
    }

    private fun metric(value: String, label: String): TextView {
        val view = body("$value\n$label", Color.WHITE)
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        view.setPadding(dp(12), dp(12), dp(12), dp(12))
        view.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).withMargins(dp(4))
        return view
    }

    private fun editText(hint: String, value: String, multiLine: Boolean = false): EditText {
        val input = EditText(this)
        input.hint = hint
        input.setText(value)
        input.minLines = if (multiLine) 3 else 1
        styleInput(input)
        return input
    }

    private fun styleInput(input: EditText) {
        input.setTextColor(dark)
        input.setHintTextColor(muted)
        input.textSize = 16f
        input.setPadding(dp(14), dp(12), dp(14), dp(12))
        input.background = rounded(Color.WHITE, line)
        input.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withMargins(0, dp(7), 0, dp(7))
    }

    private fun title(text: String, size: Int, color: Int = dark): TextView {
        val view = TextView(this)
        view.text = text
        view.setTextColor(color)
        view.textSize = size.toFloat()
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withMargins(0, dp(4), 0, dp(8))
        return view
    }

    private fun eyebrow(text: String, color: Int = green): TextView {
        val view = TextView(this)
        view.text = text.uppercase()
        view.setTextColor(color)
        view.textSize = 12f
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        return view
    }

    private fun body(text: String, color: Int = muted): TextView {
        val view = TextView(this)
        view.text = text
        view.setTextColor(color)
        view.textSize = 16f
        view.setLineSpacing(4f, 1f)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withMargins(0, dp(4), 0, dp(8))
        return view
    }

    private fun bodyCard(text: String): TextView {
        val view = body(text, dark)
        view.setPadding(dp(16), dp(14), dp(16), dp(14))
        view.background = rounded(Color.WHITE, line)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withMargins(0, dp(7), 0, dp(7))
        return view
    }

    private fun card(): LinearLayout {
        val layout = verticalLayout()
        layout.setPadding(dp(18), dp(18), dp(18), dp(18))
        layout.background = rounded(Color.WHITE, line)
        layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withMargins(0, dp(10), 0, dp(10))
        return layout
    }

    private fun primaryButton(text: String): Button {
        val button = Button(this)
        button.text = text
        button.setTextColor(Color.rgb(32, 23, 3))
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        button.background = rounded(gold, gold)
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).withMargins(0, dp(8), 0, dp(8))
        return button
    }

    private fun smallButton(text: String, solid: Boolean): Button {
        val button = Button(this)
        button.text = text
        button.setTextColor(if (solid) Color.WHITE else green)
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        button.background = rounded(if (solid) green else Color.WHITE, green)
        button.layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).withMargins(dp(4))
        return button
    }

    private fun chip(text: String, active: Boolean): Button {
        val button = Button(this)
        button.text = text
        button.setTextColor(if (active) Color.WHITE else dark)
        button.background = rounded(if (active) green else Color.WHITE, if (active) green else line, 40)
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42)).withMargins(dp(4))
        return button
    }

    private fun verticalLayout(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        return layout
    }

    private fun horizontalLayout(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        return layout
    }

    private fun wrapLayout(): LinearLayout {
        val layout = verticalLayout()
        layout.setPadding(0, dp(8), 0, dp(8))
        return layout
    }

    private fun rounded(fill: Int, stroke: Int, radius: Int = 8): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.setColor(fill)
        drawable.cornerRadius = dp(radius).toFloat()
        drawable.setStroke(dp(1), stroke)
        return drawable
    }

    private fun LinearLayout.LayoutParams.withMargins(all: Int): LinearLayout.LayoutParams {
        setMargins(all, all, all, all)
        return this
    }

    private fun LinearLayout.LayoutParams.withMargins(left: Int, top: Int, right: Int, bottom: Int): LinearLayout.LayoutParams {
        setMargins(left, top, right, bottom)
        return this
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
