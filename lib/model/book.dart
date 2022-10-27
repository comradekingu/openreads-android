import 'package:flutter/foundation.dart';

class Book {
  int? id;
  String? title;
  String? author;
  int? status;
  int? rating;
  String? startDate;
  String? finishDate;
  int? pages;
  int? publicationYear;
  String? isbn;
  String? olid;
  String? tags;
  String? myReview;
  final Uint8List? cover;
  final String? blurHash;

  Book({
    this.id,
    this.title,
    this.author,
    this.status,
    this.rating,
    this.startDate,
    this.finishDate,
    this.pages,
    this.publicationYear,
    this.isbn,
    this.olid,
    this.tags,
    this.myReview,
    this.cover,
    this.blurHash,
  });

  factory Book.fromJSON(Map<String, dynamic> json) {
    return Book(
      id: json['id'],
      title: json['title'],
      author: json['author'],
      status: json['status'],
      rating: json['rating'],
      startDate: json['start_date'],
      finishDate: json['finish_date'],
      pages: json['pages'],
      publicationYear: json['publication_year'],
      isbn: json['isbn'],
      olid: json['olid'],
      tags: json['tags'],
      myReview: json['my_review'],
      cover: json['cover'],
      blurHash: json['blur_hash'],
    );
  }

  Map<String, dynamic> toJSON() {
    return {
      'id': id,
      'title': title,
      'author': author,
      'status': status,
      'rating': rating,
      'start_date': startDate,
      'finish_date': finishDate,
      'pages': pages,
      'publication_year': publicationYear,
      'isbn': isbn,
      'olid': olid,
      'tags': tags,
      'my_review': myReview,
      'cover': cover,
      'blur_hash': blurHash,
    };
  }
}