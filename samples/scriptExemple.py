from com.playonlinux.framework.templates import Installer

class Example(Installer):
    logContext = "ExampleScript"
    title = "TITLE"

    def main(self):
        setupWindow = self.getSetupWizard()

        print "Hello from python!"

        setupWindow.message("Test\nTest")
        setupWindow.message("Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 " +
                                  "Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 " +
                                  "Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 Test 2 ")

        result = setupWindow.textbox("Test 3")
        print result

        setupWindow.message("Test 4")
        setupWindow.message("Test 5")

        setupWindow.close()

    def rollback(self):
        setupWindow = self.getSetupWizard()
        setupWindow.message("It seems that everything has crashed. Last chance to rollback")
        Installer.rollback(self)