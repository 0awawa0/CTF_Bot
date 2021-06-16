package ui.main

import db.DatabaseHelper

class MainViewModel {

    val competitions = DatabaseHelper.competitions
}