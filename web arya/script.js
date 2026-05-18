const courses = [
  {
    id: 1,
    title: "Assistant Electrician",
    trade: "Electrician",
    duration: "Long term",
    center: "Ramanagara Skill Center",
    eligibility: "10th Pass",
    seats: 24,
    stipend: "Rs. 1,500/month",
    placement: "Placement support available",
  },
  {
    id: 2,
    title: "Tailoring and Sewing Machine Operator",
    trade: "Sewing",
    duration: "Short term",
    center: "Magadi Rural Training Hub",
    eligibility: "8th Pass",
    seats: 30,
    stipend: "Toolkit support",
    placement: "Self-employment support",
  },
  {
    id: 3,
    title: "Web Coding Basics",
    trade: "Coding",
    duration: "Short term",
    center: "Channapatna Technical Institute",
    eligibility: "12th Pass",
    seats: 18,
    stipend: "Certificate support",
    placement: "Internship guidance",
  },
  {
    id: 4,
    title: "Mobile Phone Repair",
    trade: "Mobile Repair",
    duration: "Short term",
    center: "Kanakapura Livelihood Center",
    eligibility: "10th Pass",
    seats: 22,
    stipend: "Rs. 1,000/month",
    placement: "Shop setup guidance",
  },
  {
    id: 5,
    title: "Welding Technician",
    trade: "Welding",
    duration: "Long term",
    center: "Ramanagara Skill Center",
    eligibility: "10th Pass",
    seats: 32,
    stipend: "Rs. 1,800/month",
    placement: "Industry placement support",
  },
];

let activeFilter = "all";
let selectedCourse = courses[0];
let latestSummary = "";

const courseGrid = document.querySelector("#courseGrid");
const searchInput = document.querySelector("#searchInput");
const chips = document.querySelectorAll(".chip");
const form = document.querySelector("#candidateForm");
const summaryOutput = document.querySelector("#summaryOutput");
const selectedCourseLabel = document.querySelector("#selectedCourse");
const saveStatus = document.querySelector("#saveStatus");
const savedApplications = document.querySelector("#savedApplications");
const copySummary = document.querySelector("#copySummary");
const clearSaved = document.querySelector("#clearSaved");

function getSavedApplications() {
  return JSON.parse(localStorage.getItem("namma-skill-web-applications") || "[]");
}

function setSavedApplications(applications) {
  localStorage.setItem("namma-skill-web-applications", JSON.stringify(applications));
}

function updateSelectedCourse(course) {
  selectedCourse = course;
  selectedCourseLabel.textContent = `Selected course: ${course.title}`;
}

function renderCourses() {
  const search = searchInput.value.trim().toLowerCase();
  const filteredCourses = courses.filter((course) => {
    const matchesFilter =
      activeFilter === "all" ||
      course.trade === activeFilter ||
      course.duration === activeFilter;

    const haystack = `${course.title} ${course.trade} ${course.center}`.toLowerCase();
    return matchesFilter && haystack.includes(search);
  });

  courseGrid.innerHTML = filteredCourses
    .map(
      (course) => `
        <article class="course-card">
          <div>
            <p class="eyebrow">${course.trade} | ${course.duration}</p>
            <h3>${course.title}</h3>
          </div>
          <div class="course-meta">
            <span>Center: ${course.center}</span>
            <span>Eligibility: ${course.eligibility}</span>
            <span>Seats: ${course.seats}</span>
            <span>Stipend: ${course.stipend}</span>
          </div>
          <p class="muted">${course.placement}</p>
          <div class="course-actions">
            <button class="button button--outline" type="button" data-alert="${course.id}">Alert me</button>
            <button class="button button--solid" type="button" data-apply="${course.id}">
              ${selectedCourse.id === course.id ? "Selected" : "Apply"}
            </button>
          </div>
        </article>
      `,
    )
    .join("");

  if (!filteredCourses.length) {
    courseGrid.innerHTML = `<p class="muted">No courses found for this search.</p>`;
  }
}

function renderSavedApplications() {
  const applications = getSavedApplications();

  if (!applications.length) {
    savedApplications.innerHTML = `<p class="muted">No saved applications yet. Submit the form to store one here.</p>`;
    return;
  }

  savedApplications.innerHTML = applications
    .map(
      (application, index) => `
        <article class="saved-item">
          <div>
            <strong>${application.candidate.name} - ${application.course.title}</strong>
            <span>${application.candidate.village} | ${application.course.center} | ${new Date(application.savedAt).toLocaleString()}</span>
          </div>
          <button class="button button--outline" type="button" data-view-saved="${index}">View summary</button>
        </article>
      `,
    )
    .join("");
}

function buildSummary(formData) {
  return `Candidate Name: ${formData.get("name")}
Phone Number: ${formData.get("phone")}
Village: ${formData.get("village")}
Education: ${formData.get("education")}

Preferred Course: ${selectedCourse.title}
Trade and Duration: ${selectedCourse.trade} - ${selectedCourse.duration}
Training Center: ${selectedCourse.center}
Eligibility Match: Candidate education can be verified against ${selectedCourse.eligibility}
Seats Available: ${selectedCourse.seats}
Support: ${selectedCourse.stipend}; ${selectedCourse.placement}

Motivation:
${formData.get("motivation")}

Requested Action:
Trainer should call the candidate, confirm documents, and guide them for the next batch registration.`;
}

courseGrid.addEventListener("click", (event) => {
  const alertButton = event.target.closest("[data-alert]");
  const applyButton = event.target.closest("[data-apply]");

  if (alertButton) {
    const course = courses.find((item) => item.id === Number(alertButton.dataset.alert));
    alertButton.textContent = `Alert active: ${course.trade}`;
    alertButton.disabled = true;
  }

  if (applyButton) {
    updateSelectedCourse(courses.find((item) => item.id === Number(applyButton.dataset.apply)));
    renderCourses();
    document.querySelector("#apply").scrollIntoView({ behavior: "smooth" });
    summaryOutput.textContent = `Selected Course: ${selectedCourse.title}\n\nFill the candidate form and submit to generate the summary.`;
  }
});

chips.forEach((chip) => {
  chip.addEventListener("click", () => {
    chips.forEach((item) => item.classList.remove("is-active"));
    chip.classList.add("is-active");
    activeFilter = chip.dataset.filter;
    renderCourses();
  });
});

searchInput.addEventListener("input", renderCourses);

form.addEventListener("submit", (event) => {
  event.preventDefault();
  if (!form.checkValidity()) {
    form.reportValidity();
    return;
  }

  const formData = new FormData(form);
  const summary = buildSummary(formData);
  const application = {
    savedAt: new Date().toISOString(),
    course: selectedCourse,
    candidate: Object.fromEntries(formData.entries()),
    summary,
  };

  latestSummary = summary;
  summaryOutput.textContent = summary;
  setSavedApplications([application, ...getSavedApplications()].slice(0, 8));
  saveStatus.textContent = "Application saved in this browser.";
  renderSavedApplications();
});

savedApplications.addEventListener("click", (event) => {
  const viewButton = event.target.closest("[data-view-saved]");
  if (!viewButton) return;

  const application = getSavedApplications()[Number(viewButton.dataset.viewSaved)];
  updateSelectedCourse(application.course);
  latestSummary = application.summary;
  summaryOutput.textContent = application.summary;
  saveStatus.textContent = "Showing saved application summary.";
  document.querySelector("#apply").scrollIntoView({ behavior: "smooth" });
  renderCourses();
});

copySummary.addEventListener("click", async () => {
  const text = latestSummary || summaryOutput.textContent;
  await navigator.clipboard.writeText(text);
  saveStatus.textContent = "Summary copied to clipboard.";
});

clearSaved.addEventListener("click", () => {
  setSavedApplications([]);
  saveStatus.textContent = "Saved applications cleared.";
  renderSavedApplications();
});

updateSelectedCourse(selectedCourse);
renderCourses();
renderSavedApplications();
