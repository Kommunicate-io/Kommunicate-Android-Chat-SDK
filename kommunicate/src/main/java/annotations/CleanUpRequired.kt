package annotations

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.FILE,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPE
)
annotation class CleanUpRequired(
    val reason: String,
    val taskDependent: String = ""
)