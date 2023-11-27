#Import Packages
import sys
import pygame
from time import sleep
from utils import ServerSocket, DriveValue

# screen color
white = (255, 255, 255)
black = (0, 0, 0)
blue = (0, 0, 128)
red = (200, 0, 0)

# Instructions
def caution():
    """
    Display the content in Terminal window 
    """
    name= r"""
                     --- ---- ---- ---- ---- ---- ---- ---- ---- ----
                    | Note:                                          |
                    |     If you want to stop ?                      |
                    |     Click the 'X' on pygame window             |
                    |               or                               |
                    |     Press 'esc' to Quit the pygame window      |
                     --- ---- ---- ---- ---- ---- ---- ---- ---- ----

                   _________   ___            ___   _________      __________         ________
                 /  ________|  \  \          /  /  |   ____  \    |   _____  \      /  ______  \
                /  /            \  \        /  /   |  |    \  \   |  |     \  \    /  /      \  \
                |  |             \  \      /  /    |  |     |  |  |  |_____/  /   |  |        |  |
                |  |              \  \    /  /     |  |____/  /   |  |_______/    |  |        |  |
                |  |               \  \  /  /      |  |______/    |  |   \  \     |  |        |  |
                \  \________        \  \/  /       |  |           |  |    \  \     \  \ _____/  /
                 \ _________|        \____/        |__|           |__|     \__\     \ ________ /

                    """
    print(name)


def usage():
    """
    Display the control keys in Pygame window
    """
    usage_str = """
    Make sure to keep the pygame window in focus!\r

    Use the following keys to drive the robot:\r

    \tw         :   Go forward\r
    \ts         :   Go backward\r
    \ta         :   Turn slightly left (while driving)\r
    \td         :   Turn slightly right (while driving)\r
    \tesc       :   Exit\r
    """
    return usage_str

class Screen:
    """
    Pygame Window Screen 
    """
    screen = None
    font = None
    y_pos = 0
    x_pos = 0

    def setup_screen(self):
        """
        Display the Font-size and resolution for screen
        """
        pygame.display.set_caption("CVPRO Competition Controller")
        self.font = pygame.font.Font(None, 26)  # Use system font
        self.screen = pygame.display.set_mode([800, 600], pygame.RESIZABLE)
        self.screen.fill(white)
        text = usage()
        print(text)
        lines = text.strip().split("\r")
        self.x_pos = 50
        self.y_pos = 50
        delimiter = ":"
        for line in lines:
            # create a text suface object
            if delimiter in line:
                space = "         " if "\t" in line else ""
                elements = line.strip().split(delimiter)
                text = self.font.render(
                    space + elements[0].strip() + delimiter, True, blue
                )
                self.screen.blit(text, (self.x_pos, self.y_pos))
                text = self.font.render(elements[1].strip(), True, black)
                self.screen.blit(text, (self.x_pos + 200, self.y_pos))
            else:
                text = self.font.render(line, True, red)
                self.screen.blit(text, (self.x_pos, self.y_pos))
            pygame.display.update()
            self.y_pos += 40

screen = Screen()

s_socket = ServerSocket()

class CommandHandler:
    def __init__(self):
        self.left = DriveValue()
        self.right = DriveValue()

    def on_key_event(self, event):
        if event.type == pygame.KEYDOWN:
            if event.key == pygame.K_d:
                print('Right Direction')
                s_socket.send("d")
            elif event.key == pygame.K_a:
                print('Left Direction')
                s_socket.send("a")
            elif event.key == pygame.K_w:
                print('Forward Direction')
                s_socket.send("w")
            elif event.key == pygame.K_s:
                print('Backward Direction')
                s_socket.send("s")
            elif event.key == pygame.K_ESCAPE:
                print('Exit')
                sys.exit()

        if event.type == pygame.KEYUP:
            print('Key Released')
            s_socket.send("o")

def run():
    s_socket.accept()
    cmd_handler = CommandHandler()

    running = True
    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT: 
                print('Exit')
                running = False
            
            cmd_handler.on_key_event(event)

        sleep(0.1)

    pygame.quit()

if __name__ == '__main__':
    caution()
    pygame.init()
    screen.setup_screen()
    run()
