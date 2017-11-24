import java.util.Stack;
/**
 *  This class is the main class of the "World of Zuul" application. 
 *  "World of Zuul" is a very simple, text based adventure game.  Users 
 *  can walk around some scenery. That's all. It should really be extended 
 *  to make it more interesting!
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 * 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * @author  Giovanny Ospina
 * @version 11.19.2017
 */


public class Game 
{
    private Parser parser;
    private Room currentRoom;
    private Room lastRoom;
    private int timer = 0;
    private Room fail;
    private Stack multiLastRooms = new Stack();
        
    /**
     * Create the game and initialise its internal map.
     */
    public Game() 
    {
        createRooms();
        parser = new Parser();
    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms()
    {
        Room seats, bathroom, lockerroom, level1, level2, hallway, shopstand, janitors, food, supply,
                                                    stairs, tunnel, conference, field, outside, fail;
      
        // create the rooms
        seats = new Room("at the seats in the stadium.");
        bathroom = new Room("in the bathroom. Can't hide in here forever.");
        lockerroom = new Room("in the clubs locker room.");
        level1 = new Room("on the first level of the stadium. Almost there.");
        level2 = new Room("on the second level of the stadium. I need to find my way downstairs.");
        hallway = new Room("in the main hallways of the stadium. Wow it's a complete mess.");
        shopstand = new Room("at the destroyed t-shirt stand in the hallways.");
        janitors = new Room("inside the janitors closet.");
        food = new Room("at the food stand. I can easily eat right now.");
        supply = new Room("in the supply room. Can't believe this room is bigger than my house.");
        stairs = new Room("in the staircase.");
        tunnel = new Room("inside the field tunnel. This is how it feels to be a player....... nice.");
        conference = new Room("in the conference room in the stadium. ");
        field = new Room("in the middle of the field. What a view.");
        outside = new Room("finally outside. Time to go back home!!");
        fail = new Room("You have been consumed by the riot. Your great day has been torn to smithereens. You lose.");
        
        // initialise room exits
        seats.setExit("up", hallway);
        seats.setExit("down", field);
        
        bathroom.setExit("out", hallway);
        
        lockerroom.setExit("right", conference);
        lockerroom.setExit("left", supply);
        lockerroom.setExit("forward", tunnel);
        
        level1.setExit("up", stairs);
        level1.setExit("right", hallway);
        level1.setExit("left", hallway);
        level1.setExit("forward", seats);
        
        level2.setExit("down", stairs);
        level2.setExit("right", hallway);
        level2.setExit("left", hallway);
      
        hallway.setExit("right", bathroom);
        hallway.setExit("left", shopstand);
        hallway.setExit("forward", food);
        hallway.setExit("down", seats);
        
        shopstand.setExit("right", hallway);
        shopstand.setExit("left", janitors);
        shopstand.setExit("forward", food);
        
        janitors.setExit("forward", janitors);
        
        food.setExit("left", hallway);
        food.setExit("right", bathroom);
        
        supply.setExit("out", outside);
        
        stairs.setExit("up", level2);
        stairs.setExit("down", level1);
        
        tunnel.setExit("in", lockerroom);
        tunnel.setExit("out", field);
        
        conference.setExit("left", lockerroom);
        conference.setExit("right", lockerroom);
        
        field.setExit("right", tunnel);
        field.setExit("up", seats);
        
        field.addItem(new Item ("soccer ball", 2));
        lockerroom.addItem(new Item ("cleats", 1));
        shopstand.addItem(new Item("jersey" , 0.5));
        food.addItem(new Item("crunchy nachos" , 0.25));

        currentRoom = seats;  // start game at your seat
    }

    /**
     *  Main play routine.  Loops until end of play.
     */
    public void play() 
    {            
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.
                
        boolean finished = false;
        while (! finished) 
        {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
       
        if (timer > 20)
        {
            currentRoom = fail;
            System.out.println(currentRoom.getLongDescription());
            finished = true;
        }
    
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("The World Cup finals is over and the stadium is roaring!");
        System.out.println("Wait what is that? A flare has just been shot across the stadium and riots begin to break out!");
        System.out.println("Quickly get out of there unharmed.");
        System.out.println("Type '" + CommandWord.HELP + "' if you need help.");
        System.out.println();
        System.out.println(currentRoom.getLongDescription());
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) 
    {
        boolean wantToQuit = false;

        CommandWord commandWord = command.getCommandWord();

        switch (commandWord) {
            case UNKNOWN:
                System.out.println("I don't know what you mean...");
                break;

            case HELP:
                printHelp();
                break;

            case GO:
                goRoom(command);
                break;
                
            
            // case GRAB:
                // grab();
                // break;
                
            case BACK:
                back();
                break;

            case QUIT:
                wantToQuit = quit(command);
                break;
        }
        return wantToQuit;
    }

    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the 
     * command words.
     */
    private void printHelp() 
    {
        System.out.println("Riots have broken out after the game and the whole stadium is crazy.");
        System.out.println("You are at your seat watching and all you can is to get out of there.");
        System.out.println();
        System.out.println("Your command words are:");
        parser.showCommands();
    }

    /** 
     * Try to go in one direction. If there is an exit, enter the new
     * room, otherwise print an error message.
     */
    private void goRoom(Command command) 
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }

        String direction = command.getSecondWord();

        // Try to leave current room.
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no exit!");
        }
        else {
            lastRoom = currentRoom;
            currentRoom = nextRoom;
            timer = timer + 1;
            System.out.println(currentRoom.getLongDescription());
        }
    }
    
    /**
     * Back method that allows you to go to the room right before the current room.
     */
   
    private void back()
    {
        if (multiLastRooms.empty())
        {
            System.out.println("You haven't gone anywhere!");
        }
        else
        {
            currentRoom = (Room) multiLastRooms.pop();
            System.out.println("You retrace your foot steps and find your way back to where you were earlier.");
            System.out.println(currentRoom.getLongDescription());
        }
    }
    
    /** 
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command) 
    {
        if(command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        else {
            return true;  // signal that we want to quit
        }
    }
}
