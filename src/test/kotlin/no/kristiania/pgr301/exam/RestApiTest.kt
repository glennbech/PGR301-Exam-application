package no.kristiania.pgr301.exam

import com.github.javafaker.Faker
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import no.kristiania.pgr301.exam.dto.CourseDto
import no.kristiania.pgr301.exam.dto.SignUpDto
import no.kristiania.pgr301.exam.repository.ExamResultRepository
import no.kristiania.pgr301.exam.service.CourseService
import no.kristiania.pgr301.exam.service.StudentService
import no.kristiania.pgr301.exam.service.UserService
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ Application::class ], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RestApiTest{

    @LocalServerPort
    protected var port = 0

    @Autowired
    protected lateinit var userService: UserService

    @Autowired
    protected lateinit var studentService: StudentService

    @Autowired
    protected lateinit var courseService: CourseService

    protected val faker = Faker()


    @Autowired
    protected lateinit var examResultRepository: ExamResultRepository

    @PostConstruct
    fun init(){
        RestAssured.baseURI = "http://localhost"
        RestAssured.basePath = "/api"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @BeforeEach
    fun cleanUp(){
        examResultRepository.deleteAll()
        studentService.deleteAll()
        userService.deleteAll()
        courseService.deleteAll()
    }

    @Test
    fun shouldCreateCourse() {

        val dto = CourseDto("T01", "TEST1")

        given().contentType(ContentType.JSON)
                .body(dto)
                .post("/courses")
                .then()
                .statusCode(201)

        given().get("/courses")
                .then()
                .statusCode(200)
                .and()
                .body("data.list.size", greaterThan(0))
    }

    @Test
    fun shouldCreateStudent(){

        val studentId = faker.artist().name()

        val dto = SignUpDto(studentId)

        given().contentType(ContentType.JSON)
                .body(dto)
                .post("/students/signup")
                .then()
                .statusCode(201)

        given().get("/students/$studentId")
                .then()
                .statusCode(200)
                .and()
                .body("data.id", equalTo(studentId))

    }

    @Test
    fun shouldDoExam(){

        val studentId = faker.name().firstName()

        val courseCode =  "600${faker.random().nextInt(0, 9)}"
        val courseName = "Introduction to ${faker.programmingLanguage().name()}"

        val studentDto = SignUpDto(studentId)
        val courseDto = CourseDto(courseCode, courseName)

        given().contentType(ContentType.JSON)
                .body(studentDto)
                .post("/students/signup")
                .then()
                .statusCode(201)

        given().contentType(ContentType.JSON)
                .body(courseDto)
                .post("/courses")
                .then()
                .statusCode(201)

        given().put("/students/${studentId}/exams/${courseCode}")
                .then()
                .statusCode(200)
                .body("data.courseCode", equalTo(courseCode))
                .body("data.grade", notNullValue())
                .log().body()


    }


    /*
    @Test
    fun intentionalFailure(){

        val studentDto = SignUpDto(faker.name().firstName())

        given().contentType(ContentType.JSON)
                .body(studentDto)
                .post("/students/signup")
                .then()
                .statusCode(201)

        given().contentType(ContentType.JSON)
                .body(studentDto)
                .post("/students/signup")
                .then()
                .statusCode(201)
    } */


}