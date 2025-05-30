package handler;

public enum EndPoint {
    GET_TASKS,
    GET_TASKS_BY_ID,
    POST_TASKS,
    DELETE_TASKS_BY_ID,

    GET_EPICS,
    GET_EPICS_BY_ID,
    POST_EPICS,
    DELETE_EPICS_BY_ID,
    GET_EPICS_SUBTASKS_BY_ID,

    GET_SUBTASKS,
    GET_SUBTASKS_BY_ID,
    POST_SUBTASKS,
    DELETE_SUBTASKS_BY_ID,

    GET_HISTORY,
    GET_PRIORITIZED,

    UNKNOWN
}
