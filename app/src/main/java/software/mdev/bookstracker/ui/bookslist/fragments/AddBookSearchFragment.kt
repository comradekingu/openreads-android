package software.mdev.bookstracker.ui.bookslist.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import software.mdev.bookstracker.R
import software.mdev.bookstracker.data.db.BooksDatabase
import software.mdev.bookstracker.data.db.entities.Book
import software.mdev.bookstracker.data.repositories.BooksRepository
import software.mdev.bookstracker.ui.bookslist.viewmodel.BooksViewModel
import software.mdev.bookstracker.ui.bookslist.viewmodel.BooksViewModelProviderFactory
import software.mdev.bookstracker.ui.bookslist.ListActivity
import kotlinx.android.synthetic.main.fragment_add_book_search.*
import software.mdev.bookstracker.data.db.YearDatabase
import software.mdev.bookstracker.data.repositories.YearRepository
import software.mdev.bookstracker.other.Constants
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import software.mdev.bookstracker.adapters.*
import software.mdev.bookstracker.api.models.OpenLibraryBook
import software.mdev.bookstracker.api.models.OpenLibraryOLIDResponse
import software.mdev.bookstracker.data.db.LanguageDatabase
import software.mdev.bookstracker.data.db.entities.Language
import software.mdev.bookstracker.data.repositories.LanguageRepository
import software.mdev.bookstracker.data.repositories.OpenLibraryRepository
import software.mdev.bookstracker.other.Resource
import software.mdev.bookstracker.ui.bookslist.dialogs.*
import kotlin.collections.ArrayList


class AddBookSearchFragment : Fragment(R.layout.fragment_add_book_search) {

    lateinit var viewModel: BooksViewModel
    lateinit var foundBooksAdapter: FoundBookAdapter
    lateinit var languageAdapter: LanguageAdapter

    lateinit var book: Book
    lateinit var listActivity: ListActivity
    private var bookFinishDateMs: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as ListActivity).booksViewModel
        listActivity = activity as ListActivity

        var whatIsClicked = Constants.BOOK_STATUS_NOTHING

        val sharedPref = (activity as ListActivity).getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val database = BooksDatabase(view.context)
        val yearDatabase = YearDatabase(view.context)
        val languageDatabase = LanguageDatabase(view.context)

        val repository = BooksRepository(database)
        val yearRepository = YearRepository(yearDatabase)
        val openLibraryRepository = OpenLibraryRepository()
        val languageRepository = LanguageRepository(languageDatabase)

        val booksViewModelProviderFactory = BooksViewModelProviderFactory(
            repository,
            yearRepository,
            openLibraryRepository,
            languageRepository
        )

        var accentColor = getAccentColor(view.context.applicationContext)

        rvLanguages.visibility = View.GONE

        etAdderBookTitleSearch.requestFocus()
        showKeyboard(etAdderBookTitleSearch, 350)

        setupRvLanguages()
        setupRvFoundBooks()

        var searchQueryJob: Job? = null
        var searchQueryAutoJob: Job? = null
        var searchByOLIDJob: Job? = null
        var searchAuthorJob: Job? = null


        if (sharedPref.getBoolean(Constants.SHARED_PREFERENCES_KEY_SHOW_OL_ALERT, true)) {
            AlertDialog(
                view,
                object : AlertDialogListener {
                    override fun onOkButtonClicked(isChecked: Boolean) {
                        if (isChecked) {
                            editor.apply {
                                putBoolean(Constants.SHARED_PREFERENCES_KEY_SHOW_OL_ALERT, false)
                                apply()
                            }
                        }
                    }
                }, activity as ListActivity
            ).show()
        }

        btnFilterLanguage.setOnClickListener {
            if (rvLanguages.visibility == View.GONE) {
                rvLanguages.visibility = View.VISIBLE
                rvLanguages.scrollToPosition(0)

                val currentLayout = frameLayout2.layoutParams as ConstraintLayout.LayoutParams
                currentLayout.topToBottom = R.id.frLanguages
                frameLayout2.layoutParams = currentLayout
            }
            else {
                rvLanguages.visibility = View.GONE

                val currentLayout = frameLayout2.layoutParams as ConstraintLayout.LayoutParams

                currentLayout.topToBottom = R.id.btnFilterLanguage
                frameLayout2.layoutParams = currentLayout
            }
        }

        etAdderBookTitleSearch.addTextChangedListener { editable ->
            searchQueryJob?.cancel()
            searchQueryAutoJob?.cancel()
            searchByOLIDJob?.cancel()
            searchAuthorJob?.cancel()

            viewModel.openLibrarySearchResult.value = null
            viewModel.openLibraryBooksByOLID.value = null

            searchQueryJob?.cancel()
            searchQueryAutoJob?.cancel()
            searchByOLIDJob?.cancel()
            searchAuthorJob?.cancel()

            searchQueryAutoJob = MainScope().launch {
                delay(500L)

                var selectedLanguages = viewModel.selectedLanguages.value
                if (selectedLanguages != null) {
                    for (selectedLanguage in selectedLanguages) {
                        var oldCounter = selectedLanguage.selectCounter

                        if (oldCounter == null)
                            viewModel.updateCounter(selectedLanguage.id,  1)
                        else
                            viewModel.updateCounter(selectedLanguage.id,  oldCounter + 1)
                    }
                }

                editable?.let {
                    if (it.isNotEmpty()) {
                        var searchQuery = it.toString()

                        if (searchQuery.last().toString() == " ")
                            searchQuery = searchQuery.dropLast(1)

                        viewModel.searchBooksInOpenLibrary(searchQuery, context)
                    }
                }
            }
        }

        btnSearchInOL.setOnClickListener {
            it.hideKeyboard()

            searchQueryJob?.cancel()
            searchQueryAutoJob?.cancel()
            searchByOLIDJob?.cancel()
            searchAuthorJob?.cancel()
            var editable = etAdderBookTitleSearch.text.toString()

            viewModel.openLibrarySearchResult.value = null
            viewModel.openLibraryBooksByOLID.value = null

            searchQueryJob?.cancel()
            searchQueryAutoJob?.cancel()
            searchByOLIDJob?.cancel()
            searchAuthorJob?.cancel()

            searchQueryJob = MainScope().launch {

                var selectedLanguages = viewModel.selectedLanguages.value
                if (selectedLanguages != null) {
                    for (selectedLanguage in selectedLanguages) {
                        var oldCounter = selectedLanguage.selectCounter

                        if (oldCounter == null)
                            viewModel.updateCounter(selectedLanguage.id,  1)
                        else
                            viewModel.updateCounter(selectedLanguage.id,  oldCounter + 1)
                    }
                }

                editable?.let {
                    if (editable.isNotEmpty()) {
                        if (editable.last().toString() == " ")
                            editable.dropLast(1)
                        viewModel.searchBooksInOpenLibrary(editable, context)
                    }
                }
            }
        }

        viewModel.openLibrarySearchResult.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { booksResponse ->
                        var booksResponseCleaned: MutableList<OpenLibraryBook>? =
                            ArrayList<OpenLibraryBook>()

                        for (item in booksResponse.docs) {
                            if (item.title != null || item.author_name != null) {
                                booksResponseCleaned?.add(item)
                            }
                        }

                        if (booksResponseCleaned != null) {
                            searchByOLIDJob = viewModel.getBooksByOLID(booksResponseCleaned, context)
                        }
                    }
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        })

        viewModel.getLanguages().observe(viewLifecycleOwner, Observer { languages ->
            var newList: List<Language> = emptyList()

            for (language in languages) {
                if (language.isSelected == 1) {
                    newList += language
                }
            }

            viewModel.selectedLanguages.postValue(newList)

            var selectedLanguages = newList.size
            if (selectedLanguages == 0) {
                btnFilterLanguage.text = getString(R.string.button_language)
            } else {
                var buttonText = getString(R.string.button_language) + " (" + selectedLanguages.toString() + ")"
                btnFilterLanguage.text = buttonText
            }

        })

        var filterBooksByLanguage: Job? = null

        viewModel.openLibraryBooksByOLID.observe(viewLifecycleOwner, Observer { list ->

            viewModel.selectedLanguages.observe(viewLifecycleOwner, Observer { languages ->

                filterBooksByLanguage?.cancel()
                filterBooksByLanguage = MainScope().launch {
                    var newList: List<Resource<OpenLibraryOLIDResponse>> = emptyList()

                    if (list != null) {
                        for (item in list) {
                            if (item.data != null) {
                                if (item.data.title != null) {

                                    var add = false
                                    if (languages.isNotEmpty()) {

                                        if (item.data.languages != null) {

                                            for (language in languages) {

                                                if (language.isSelected == 1) {

                                                    for (itemLanguage in item.data.languages) {

                                                        if (language.language6392B == itemLanguage.key.replace(
                                                                "/languages/",
                                                                ""
                                                            )
                                                        ) {
                                                            add = true
                                                        }
                                                    }
                                                } else {
                                                    add = true
                                                }
                                            }
                                        }
                                    } else {
                                        add = true
                                    }

                                    for (currentItem in newList) {
                                        if (currentItem.data?.key == item.data.key)
                                            add = false
                                    }

                                    if (add)
                                        newList += item
                                }
                            }
                        }
                    }

                    foundBooksAdapter.differ.submitList(
                        newList
                    )

                    var numberOfFilteredBooks = newList.size

                    when (numberOfFilteredBooks) {
                        0 -> tvResultsNumber.text = ""
                        1 -> tvResultsNumber.text = "1 " + getString(R.string.results_1)
                        2 -> tvResultsNumber.text = "2 " + getString(R.string.results_2_4)
                        3 -> tvResultsNumber.text = "3 " + getString(R.string.results_2_4)
                        4 -> tvResultsNumber.text = "4 " + getString(R.string.results_2_4)
                        else -> tvResultsNumber.text = newList.size.toString() + " " + getString(R.string.results_5_)
                    }
                }
            })
        })

        viewModel.getLanguages().observe(viewLifecycleOwner, Observer { languages ->
            languageAdapter.differ.submitList(languages)})

        var hideProgressBarJob: Job? = null

        viewModel.showLoadingCircle.observe(viewLifecycleOwner, Observer { bool ->
            if (bool) {
                hideProgressBarJob?.cancel()
                showProgressBar()
            }
            else {
                hideProgressBarJob?.cancel()
                hideProgressBarJob = MainScope().launch {
                    delay(500L)
                    if (isActive) {
                        hideProgressBar()
                    }
                }
            }
        })

        foundBooksAdapter.setOnBookClickListener {

            AddFoundBookDialog(it, view.context,
                object: AddFoundBookDialogListener {
                    override fun onSaveButtonClicked(item: Book) {
                        viewModel.upsert(item)
                        recalculateChallenges()

                        when(item.bookStatus) {
                            Constants.BOOK_STATUS_READ -> { findNavController().navigate(
                                R.id.action_addBookSearchFragment_to_readFragment
                            )
                            }
                            Constants.BOOK_STATUS_IN_PROGRESS -> { findNavController().navigate(
                                R.id.action_addBookSearchFragment_to_inProgressFragment
                            )
                            }
                            Constants.BOOK_STATUS_TO_READ -> { findNavController().navigate(
                                R.id.action_addBookSearchFragment_to_toReadFragment
                            )
                            }
                        }
                    }
                }
            ).show()
        }
    }

    private fun setupRvFoundBooks() {
        foundBooksAdapter = FoundBookAdapter(viewModel)

        rvFoundBooks.apply {
            adapter = foundBooksAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // bounce effect on the recyclerview
        OverScrollDecoratorHelper.setUpOverScroll(
            rvFoundBooks,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
    }

    private fun setupRvLanguages() {
        languageAdapter = LanguageAdapter(this)

        rvLanguages.apply {
            adapter = languageAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    fun View.hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun View.showKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInputFromWindow(windowToken, 0, 0)
    }

    fun showKeyboard(et: EditText, delay: Long) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val inputManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(et, 0)
            }
        }, delay)
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMM yyyy")
        return format.format(date)
    }

    fun getDateFromDatePickerInMillis(datePicker: DatePicker): Long? {
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        return calendar.timeInMillis
    }

    fun getAccentColor(context: Context): Int {
        var accentColor = ContextCompat.getColor(context, R.color.green_500)

        val sharedPref = (activity as ListActivity).getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        var accent = sharedPref.getString(
            Constants.SHARED_PREFERENCES_KEY_ACCENT,
            Constants.THEME_ACCENT_DEFAULT
        ).toString()

        when (accent) {
            Constants.THEME_ACCENT_LIGHT_GREEN -> accentColor =
                ContextCompat.getColor(context, R.color.light_green)
            Constants.THEME_ACCENT_ORANGE_500 -> accentColor =
                ContextCompat.getColor(context, R.color.orange_500)
            Constants.THEME_ACCENT_CYAN_500 -> accentColor =
                ContextCompat.getColor(context, R.color.cyan_500)
            Constants.THEME_ACCENT_GREEN_500 -> accentColor =
                ContextCompat.getColor(context, R.color.green_500)
            Constants.THEME_ACCENT_BROWN_400 -> accentColor =
                ContextCompat.getColor(context, R.color.brown_400)
            Constants.THEME_ACCENT_LIME_500 -> accentColor =
                ContextCompat.getColor(context, R.color.lime_500)
            Constants.THEME_ACCENT_PINK_300 -> accentColor =
                ContextCompat.getColor(context, R.color.pink_300)
            Constants.THEME_ACCENT_PURPLE_500 -> accentColor =
                ContextCompat.getColor(context, R.color.purple_500)
            Constants.THEME_ACCENT_TEAL_500 -> accentColor =
                ContextCompat.getColor(context, R.color.teal_500)
            Constants.THEME_ACCENT_YELLOW_500 -> accentColor =
                ContextCompat.getColor(context, R.color.yellow_500)
        }
        return accentColor
    }

    fun convertLongToYear(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy")
        return format.format(date)
    }

    private fun recalculateChallenges() {
        viewModel.getSortedBooksByDateDesc(Constants.BOOK_STATUS_READ)
            .observe(viewLifecycleOwner, Observer { books ->
                var year: Int
                var years = listOf<Int>()

                for (item in books) {
                    if (item.bookFinishDate != "null" && item.bookFinishDate != "none") {
                        year = convertLongToYear(item.bookFinishDate.toLong()).toInt()
                        if (year !in years) {
                            years = years + year
                        }
                    }
                }

                for (item_year in years) {
                    var booksInYear = 0

                    for (item_book in books) {
                        if (item_book.bookFinishDate != "none" && item_book.bookFinishDate != "null") {
                            year = convertLongToYear(item_book.bookFinishDate.toLong()).toInt()
                            if (year == item_year) {
                                booksInYear++
                            }
                        }
                    }
                    viewModel.updateYearsNumberOfBooks(item_year.toString(), booksInYear)
                }
            }
            )
        lifecycleScope.launch {
            delay(500L)
            view?.hideKeyboard()
            findNavController().popBackStack()
            findNavController().popBackStack()
        }
    }
}
