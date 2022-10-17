import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:image_picker/image_picker.dart';
import 'package:openreads/bloc/book_bloc.dart';
import 'package:openreads/model/book.dart';
import 'package:openreads/ui/add_book_screen/widgets/widgets.dart';

class AddBook extends StatefulWidget {
  const AddBook({
    Key? key,
    required this.topPadding,
  }) : super(key: key);

  final double topPadding;

  @override
  State<AddBook> createState() => _AddBookState();
}

class _AddBookState extends State<AddBook> {
  final _titleController = TextEditingController();
  final _authorController = TextEditingController();
  final _pagesController = TextEditingController();
  final _pubYearController = TextEditingController();
  final _isbnController = TextEditingController();
  final _olidController = TextEditingController();
  final _myReviewController = TextEditingController();

  final _animDuration = const Duration(milliseconds: 150);
  final _flex = [1, 1, 1, 1];
  final _colors = [
    Colors.black87,
    Colors.black87,
    Colors.black87,
    Colors.black87,
  ];

  final _defaultHeight = 60.0;
  late int _status;
  int? _rating;
  DateTime? _startDate;
  DateTime? _finishDate;

  XFile? photoXFile;
  CroppedFile? croppedPhoto;
  CroppedFile? croppedPhotoPreview;

  //TODO: implement new book validation
  bool _validate() {
    return true;
  }

  void _saveBook() async {
    if (!_validate()) return;

    final cover = await croppedPhoto!.readAsBytes();

    //TODO: finish all parameters
    bookBloc.addBook(Book(
      title: _titleController.text,
      author: _authorController.text,
      status: _status,
      rating: (_rating == null) ? 0 : _rating,
      startDate: _startDate?.toIso8601String(),
      finishDate: _finishDate?.toIso8601String(),
      pages: _pagesController.text.isEmpty
          ? null
          : int.parse(_pagesController.text),
      publicationYear: _pubYearController.text.isEmpty
          ? null
          : int.parse(_pubYearController.text),
      isbn: _isbnController.text.isEmpty ? null : _isbnController.text,
      olid: _olidController.text.isEmpty ? null : _olidController.text,
      // tags: _tags,
      myReview: _myReviewController.text,
      cover: cover,
    ));
    
    if (!mounted) return;
    Navigator.pop(context);
  }

  void _loadCover() async {
    FocusManager.instance.primaryFocus?.unfocus();
    photoXFile = await ImagePicker().pickImage(
      source: ImageSource.gallery,
    );

    if (photoXFile == null) return;

    croppedPhoto = await ImageCropper().cropImage(
      maxWidth: 1024,
      maxHeight: 1024,
      sourcePath: photoXFile!.path,
      aspectRatio: const CropAspectRatio(ratioX: 2, ratioY: 3),
      compressQuality: 90,
      uiSettings: [
        AndroidUiSettings(
          toolbarTitle: 'Edit cover',
          toolbarColor: Colors.black,
          statusBarColor: Colors.black,
          toolbarWidgetColor: Colors.white,
          backgroundColor: Colors.black87,
          cropGridColor: Colors.black87,
          activeControlsWidgetColor: Colors.teal,
          cropFrameColor: Colors.black87,
          initAspectRatio: CropAspectRatioPreset.ratio3x2,
          lockAspectRatio: false,
          hideBottomControls: false,
        ),
      ],
    );

    if (croppedPhoto == null) return;

    setState(() {
      croppedPhotoPreview = croppedPhoto;
    });
  }

  void _changeStatus(int position) {
    _status = position;
    _changeStatusFlex(position);
  }

  void _changeStatusFlex(int position) {
    FocusManager.instance.primaryFocus?.unfocus();

    _flex.asMap().forEach((index, value) {
      if (position == index) {
        setState(() {
          _flex[index] = 2;
          _colors[index] = Colors.teal;
        });
      } else {
        setState(() {
          _flex[index] = 1;
          _colors[index] = Colors.black87;
        });
      }
    });
  }

  @override
  void dispose() {
    _titleController.dispose();
    _authorController.dispose();
    _pagesController.dispose();
    _pubYearController.dispose();
    _isbnController.dispose();
    _olidController.dispose();
    _myReviewController.dispose();
    super.dispose();
  }

  Row _buildStatusRow(List<double> widths) {
    return Row(
      children: [
        AnimatedStatusButton(
            duration: _animDuration,
            width: widths[0],
            height: _defaultHeight,
            icon: Icons.done,
            text: 'Done',
            color: _colors[0],
            onPressed: () {
              _changeStatus(0);
            }),
        const SizedBox(width: 10),
        AnimatedStatusButton(
            duration: _animDuration,
            width: widths[1],
            height: _defaultHeight,
            icon: Icons.autorenew,
            text: 'Reading',
            color: _colors[1],
            onPressed: () {
              _changeStatus(1);
            }),
        const SizedBox(width: 10),
        AnimatedStatusButton(
            duration: _animDuration,
            width: widths[2],
            height: _defaultHeight,
            icon: Icons.timelapse,
            text: 'For later',
            color: _colors[2],
            onPressed: () {
              _changeStatus(2);
            }),
        const SizedBox(width: 10),
        AnimatedStatusButton(
            duration: _animDuration,
            width: widths[3],
            height: _defaultHeight,
            icon: Icons.not_interested,
            text: 'Unfinished',
            color: _colors[3],
            onPressed: () {
              _changeStatus(3);
            }),
      ],
    );
  }

  Row _buildDateRow(BuildContext context) {
    return Row(
      children: [
        SetDateButton(
          defaultHeight: _defaultHeight,
          icon: Icons.timer_outlined,
          text: (_startDate == null)
              ? 'Start Date'
              : '${_startDate?.day}/${_startDate?.month}/${_startDate?.year}',
          onPressed: () async {
            FocusManager.instance.primaryFocus?.unfocus();

            final startDate = await showDatePicker(
              context: context,
              initialDate: DateTime.now(),
              firstDate: DateTime(1970),
              lastDate: DateTime.now(),
              helpText: 'Select reading start date',
            );

            if (startDate != null) {
              setState(() {
                _startDate = startDate;
              });
            }
          },
        ),
        const SizedBox(width: 10),
        SetDateButton(
          defaultHeight: _defaultHeight,
          icon: Icons.timer_off_outlined,
          text: (_finishDate == null)
              ? 'Finish Date'
              : '${_finishDate?.day}/${_finishDate?.month}/${_finishDate?.year}',
          onPressed: () async {
            FocusManager.instance.primaryFocus?.unfocus();

            final finishDate = await showDatePicker(
              context: context,
              initialDate: DateTime.now(),
              firstDate: DateTime(1970),
              lastDate: DateTime.now(),
              helpText: 'Select reading finish date',
            );

            if (finishDate != null) {
              setState(() {
                _finishDate = finishDate;
              });
            }
          },
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    double width = MediaQuery.of(context).size.width - 50;
    List<double> widths = [
      (_flex[0] * width) / (_flex[0] + _flex[1] + _flex[2] + _flex[3]),
      (_flex[1] * width) / (_flex[0] + _flex[1] + _flex[2] + _flex[3]),
      (_flex[2] * width) / (_flex[0] + _flex[1] + _flex[2] + _flex[3]),
      (_flex[3] * width) / (_flex[0] + _flex[1] + _flex[2] + _flex[3])
    ];

    return Padding(
      padding: EdgeInsets.only(top: widget.topPadding),
      child: Scaffold(
        backgroundColor: Colors.grey.shade100,
        appBar: AppBar(
          title: const Text('Add new book'),
          backgroundColor: Colors.grey.shade100,
          surfaceTintColor: Colors.grey.shade400,
          actions: [
            //TODO: change button color based on validation
            TextButton(
              onPressed: _saveBook,
              child: const Text(
                'Save',
                style: TextStyle(fontSize: 18),
              ),
            )
          ],
        ),
        body: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(10.0),
            child: Column(
              children: <Widget>[
                (croppedPhotoPreview == null)
                    ? CoverPlaceholder(
                        defaultHeight: _defaultHeight,
                        onPressed: _loadCover,
                      )
                    : CoverView(
                        croppedPhotoPreview: croppedPhotoPreview,
                        onPressed: _loadCover,
                      ),
                const SizedBox(height: 15.0),
                BookTextField(
                  controller: _titleController,
                  hint: 'Enter a title',
                  icon: Icons.book,
                  keyboardType: TextInputType.text,
                  autofocus: true,
                  maxLines: 5,
                  maxLength: 255,
                ),
                const SizedBox(height: 15.0),
                BookTextField(
                  controller: _authorController,
                  hint: 'Enter an author',
                  icon: Icons.person,
                  keyboardType: TextInputType.name,
                  maxLines: 5,
                  maxLength: 255,
                ),
                const SizedBox(height: 15.0),
                _buildStatusRow(widths),
                const SizedBox(height: 15.0),
                Container(
                  width: double.infinity,
                  height: _defaultHeight,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(5),
                  ),
                  child: Center(
                    child: RatingBar.builder(
                      initialRating: 0,
                      allowHalfRating: true,
                      glow: false,
                      unratedColor: Colors.black12,
                      glowRadius: 1,
                      itemSize: 42,
                      itemPadding: const EdgeInsets.all(5),
                      wrapAlignment: WrapAlignment.center,
                      itemBuilder: (_, __) => Icon(
                        Icons.star,
                        color: Colors.amber.shade300,
                      ),
                      onRatingUpdate: (rating) {
                        _rating = (rating * 10).toInt();
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 15.0),
                _buildDateRow(context),
                const SizedBox(height: 15.0),
                Row(
                  children: [
                    Expanded(
                      child: BookTextField(
                        controller: _pagesController,
                        hint: 'Number of pages',
                        icon: Icons.numbers,
                        keyboardType: TextInputType.number,
                        inputFormatters: <TextInputFormatter>[
                          FilteringTextInputFormatter.digitsOnly
                        ],
                        maxLength: 10,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: BookTextField(
                        controller: _pubYearController,
                        hint: 'Publication year',
                        icon: Icons.calendar_month,
                        keyboardType: TextInputType.number,
                        inputFormatters: <TextInputFormatter>[
                          FilteringTextInputFormatter.digitsOnly
                        ],
                        maxLength: 4,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 15.0),
                BookTextField(
                  controller: _isbnController,
                  hint: 'ISBN',
                  icon: Icons.pin,
                  keyboardType: TextInputType.number,
                  inputFormatters: <TextInputFormatter>[
                    FilteringTextInputFormatter.digitsOnly
                  ],
                  maxLength: 20,
                ),
                const SizedBox(height: 15.0),
                BookTextField(
                  controller: _olidController,
                  hint: 'Open Library ID',
                  icon: Icons.pin,
                  keyboardType: TextInputType.text,
                  maxLength: 20,
                ),
                const SizedBox(height: 15.0),
                Container(
                  height: _defaultHeight,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 10,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(5),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: const [
                          Icon(Icons.sell),
                          SizedBox(width: 10),
                          Text(
                            'Tags',
                            style: TextStyle(fontSize: 14),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 15.0),
                BookTextField(
                  controller: _myReviewController,
                  hint: 'My review',
                  icon: Icons.message,
                  keyboardType: TextInputType.multiline,
                  maxLength: 5000,
                  hideCounter: false,
                  maxLines: 15,
                ),
                const SizedBox(height: 25.0),
                Row(
                  children: [
                    Expanded(
                      flex: 1,
                      child: MaterialButton(
                        color: Colors.grey,
                        onPressed: () => Navigator.pop(context),
                        child: const Center(
                          child: Text("Cancel"),
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      flex: 2,
                      child: MaterialButton(
                        color: Colors.teal,
                        onPressed: _saveBook,
                        child: const Center(
                          child: Text("Add"),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 25.0),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
