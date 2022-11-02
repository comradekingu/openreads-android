part of 'open_library_bloc.dart';

abstract class OpenLibraryEvent extends Equatable {
  const OpenLibraryEvent();
}

class LoadApiEvent extends OpenLibraryEvent {
  final String query;
  final int offset;
  const LoadApiEvent(this.query, this.offset);

  @override
  List<Object?> get props => [query];
}

class ReadyEvent extends OpenLibraryEvent {
  @override
  List<Object?> get props => [];
}

class NoInternetEvent extends OpenLibraryEvent {
  @override
  List<Object?> get props => [];
}