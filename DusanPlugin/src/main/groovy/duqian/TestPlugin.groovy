import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        println("====== buildSrc TestPlugin Plugin加载===========")
        //执行自定义的  task
        project.task("TestPlugin"){
            doLast {
                println("buildSrc TestPlugin task 任务执行")
            }
        }


        target.task("hello"){
            doLast {
                println("Hello from the GreetingPlugin")
            }
        }

        project.task('hello2') { //名字为 hello 的task
            doLast {
                //获取 extension 配置信息
                println "${extension.message} from ${extension.greeter}"
            }
        }
    }
}

// 配置 extension
/*
greeting{
    greeter = 'Gradle'
    message = "Hi"
}*/
