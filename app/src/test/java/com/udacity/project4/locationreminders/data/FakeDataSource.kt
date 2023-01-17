package com.udacity.project4.locationreminders.data


import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //   Create a fake data source to act as a double to the real data source
    var remindersDaoList: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError)
            return Result.Error("Tasks not found")
        else
            return Result.Success(remindersDaoList.values.toList())

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersDaoList[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Tasks not found")
        }

        val reminder = remindersDaoList[id]

        if(reminder != null)
            return Result.Success(reminder)

        return Result.Error("Tasks not found")
    }

    override suspend fun deleteAllReminders() {
        remindersDaoList.clear()
    }
}