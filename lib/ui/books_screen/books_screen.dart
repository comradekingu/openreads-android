import 'package:flutter/material.dart';
import 'package:openreads/bloc/book_bloc.dart';
import 'package:openreads/model/book.dart';
import 'package:openreads/ui/book_screen/book_screen.dart';
import 'package:openreads/ui/books_screen/widgets/widgets.dart';
import 'package:openreads/ui/add_book_screen/widgets/widgets.dart';

class BooksScreen extends StatelessWidget {
  const BooksScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final statusBarHeight = MediaQuery.of(context).padding.top;

    bookBloc.getAllBooks();

    return Scaffold(
      extendBodyBehindAppBar: true,
      backgroundColor: Colors.grey.shade50,
      appBar: AppBar(
        title: const Text('Openreads Flutter'),
        backgroundColor: Colors.grey.shade50,
        surfaceTintColor: Colors.grey.shade400,
      ),
      floatingActionButton: Padding(
        padding: const EdgeInsets.only(bottom: 50),
        child: FloatingActionButton(
          child: const Icon(Icons.add),
          onPressed: () {
            showModalBottomSheet(
                context: context,
                isScrollControlled: true,
                builder: (context) {
                  return AddBook(
                    topPadding: statusBarHeight,
                  );
                });
          },
        ),
      ),
      body: DefaultTabController(
          length: 3,
          child: Column(
            children: [
              Expanded(
                child: TabBarView(
                  children: [
                    StreamBuilder<List<Book>>(
                      stream: bookBloc.allBooks,
                      builder: (context, AsyncSnapshot<List<Book>> snapshot) {
                        if (snapshot.hasData) {
                          if (snapshot.data == null || snapshot.data!.isEmpty) {
                            return const Center(child: Text('No books'));
                          }

                          return ListView.builder(
                            itemCount: snapshot.data!.length,
                            itemBuilder: (context, index) {
                              return BookCard(
                                book: snapshot.data![index],
                                onPressed: () {
                                  if (snapshot.data![index].id == null) return;

                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (context) => BookScreen(
                                              id: snapshot.data![index].id!,
                                            )),
                                  );
                                },
                              );
                            },
                          );
                        } else if (snapshot.hasError) {
                          return Text(snapshot.error.toString());
                        } else {
                          return const Center(
                            child: CircularProgressIndicator(),
                          );
                        }
                      },
                    ),
                    ListView(
                      padding: EdgeInsets.only(
                          top: MediaQuery.of(context).padding.top +
                              AppBar().preferredSize.height +
                              10,
                          bottom: 70),
                      children: [],
                    ),
                    ListView(
                      padding: EdgeInsets.only(
                          top: MediaQuery.of(context).padding.top +
                              AppBar().preferredSize.height +
                              10,
                          bottom: 70),
                      children: [],
                    ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 10),
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const TabBar(
                    unselectedLabelColor: Colors.black,
                    labelColor: Colors.teal,
                    tabs: [
                      Tab(
                        child: Text('Finished'),
                      ),
                      Tab(
                        child: Text('In progress'),
                      ),
                      Tab(
                        child: Text('For later'),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          )),
    );
  }
}