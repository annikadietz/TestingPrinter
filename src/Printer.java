import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.lang.Math;


public class Printer {
    private double inkLevel;
    private int paperAmount;
    private int stapleAmount;
    private double originalInk;
    private int originalPaper;
    private int originalStaple;

    public Printer() {
        inkLevel = 100;
        originalInk = 100;
        paperAmount = 500;
        originalPaper = 500;
        stapleAmount = 100;
        originalStaple = 100;
    }

    public String print(Document document, Setting settings) {
        String output = "";
        if(isFilePrintable(document, settings)) {
            //show Confirmation message
            String[] options = {"Confirm and print", "Cancel"};

            int confirmationResult = JOptionPane.showOptionDialog(null, getPrintedDescriptionString(settings, true),
                    "Confirmation",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if(confirmationResult == 0) {
                //Print file
                output = getPrintedDescriptionString(settings, false);
                saveFileToDirectory(document, settings, "printed/");
                updateLevels(document, settings);
                checkLevels();
            }
            else {
                output = "Printing was cancelled.";
            }

        }
        else {
            output = "The file cannot be printed at the moment.";
        }
        return output;
    }

    public String scan(String filePath) {
        String output = "";
        Path path = Paths.get(filePath);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String currentLine = null;
            String documentText = "";
            int lines = 0;
            while((currentLine = reader.readLine()) != null) {
                documentText += currentLine + "\r\n";
                lines ++;
            }

            File file = new File(filePath);
            String nameParts = file.getName();
            int pos = nameParts.indexOf('.');
            String documentName = nameParts.substring(0, pos) + "Scan";

            int pages = lines/30;

            Document document = new Document(pages, documentText, documentName);
            Setting setting = new Setting();

            saveScan(document, "scanned/");

            output = "Scan was successful!";

        }
        catch (IOException e ) {
            output = "Can't read the file.";
        }
        return output;
    }

    public String fillInk() {
        inkLevel = 100;
        return "Ink was filled.";
    }

    public String fillPaper() {
        paperAmount = 500;
        return "Paper was filled.";
    }

    public String fillStaples() {
        stapleAmount = 100;
        return "Staples were filled.";
    }

    private boolean isFilePrintable(Document document, Setting settings) {
        if(checkInkLevel(document, settings) && checkPaperAmount(document, settings) && checkStapleAmount(settings)) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean checkInkLevel(Document document, Setting settings) {
        //One copy needs 0.2% of the ink level
        if(document.getAmountPages() * settings.getAmount() * 0.2 <= inkLevel) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean checkPaperAmount(Document document, Setting settings) {
        if(!settings.isDoubleSided()) {
            if(paperAmount >= settings.getAmount() * document.getAmountPages()) {
                return true;
            }
        }
        else {
            if(paperAmount >= settings.getAmount() * document.getAmountPages() / 2) {
                return true;
            }
        }
        return false;
    }

    private boolean checkStapleAmount(Setting settings) {
        if(!settings.isStapled()) {
            return true;
        }
        else {
            if(settings.getAmount() <= stapleAmount) {
                return true;
            }
        }

        return false;
    }

    private void checkLevels() {
        checkPaper();
        checkInk();
        checkStaples();
    }

    private void checkPaper() {
        if(paperAmount <= 0.1 * originalPaper) {
            if(paperAmount == 0) {
                sendMaintenanceMail("Paper", true);
            }
            else {
                sendMaintenanceMail("Paper", false);
            }
        }
    }

    private void checkInk() {
        if(inkLevel <= 0.1 * originalInk) {
            if(inkLevel == 0) {
                sendMaintenanceMail("Ink", true);
            }
            else {
                sendMaintenanceMail("Ink", false);
            }
        }
    }

    private void checkStaples() {
        if(stapleAmount <= 0.1 * originalStaple) {
            if(stapleAmount == 0) {
                sendMaintenanceMail("Staple", true);
            }
            else {
                sendMaintenanceMail("Staple", false);
            }
        }
    }

    private void updateLevels(Document document, Setting settings) {
        updateInkLevel(document, settings);
        updatePaperLevel(document, settings);
        updateStapleAmount(document, settings);
    }

    private void updateInkLevel(Document document, Setting setting) {
        inkLevel = inkLevel - document.getAmountPages() * setting.getAmount() * 0.2;
        inkLevel = inkLevel*100;
        inkLevel = Math.round(inkLevel);
        inkLevel = inkLevel /100;
    }

    private void updatePaperLevel(Document document, Setting setting) {
        if(setting.isDoubleSided()) {
            paperAmount = paperAmount - ((1 + (document.getAmountPages() / 2)) * setting.getAmount());
        }
        else {
            paperAmount = paperAmount - (document.getAmountPages() * setting.getAmount());
        }
    }

    private void updateStapleAmount(Document document, Setting setting) {
        System.out.println("Pages amount: " + document.getAmountPages());
        if(setting.isStapled() && document.getAmountPages() > 1) {
            stapleAmount = stapleAmount - setting.getAmount();
        }
    }

    private void sendMaintenanceMail(String material, boolean empty) {
        Email email = new Email("Maintenance", "Report: \r\n");
        if(empty) {
            email.setMailContent(email.getMailContent() + "The " + material + " is empty.");
        }
        else {

            email.setMailContent(email.getMailContent() + "The " + material + " level is below 10%.");
        }
        placeEmail(email);
    }

    public void sendScanToMail(String fileName, String receiver) {
        String emailText = "You received the following scan: " + fileName;
        Email mail = new Email(receiver, emailText);
        placeEmail(mail);
    }

    private void placeEmail(Email mail) {
        String path =  "email/" + mail.getReceiver()  + System.currentTimeMillis() + ".txt";
        boolean flag = false;
        File printedFile = new File(path);
        Document mailDocument = new Document(1, mail.getReceiver() + "\r\n" + mail.getMailContent(), mail.getReceiver()  + System.currentTimeMillis());

        try {
            flag = printedFile.createNewFile();
        } catch (IOException ioe) {
            System.out.println("Error while creating the file " + ioe);
        }

        if (flag) {
            try {
                writeToFile(path, mailDocument);
            } catch (IOException ioe) {
                System.out.println("Error while writing to the file " + ioe);
            }
        }
    }

    private String getPrintedDescriptionString(Setting settings, boolean inFuture) {
        String output = "";
        String lineSeperator = "\r\n";
        if(inFuture) {
            output = "The file will be printed with the following settings: " + lineSeperator;
        }
        else {
            output = "The file was printed with the following settings: " + lineSeperator;
        }
        output += "     - Size: " + settings.getSize() + lineSeperator;
        output += "     - Amount: " + settings.getAmount() + lineSeperator;
        if(settings.isColoured()) {
            output += "     - Coloured " + lineSeperator;
        }
        else {
            output += "     - Black and White " + lineSeperator;
        }
        if(settings.isDoubleSided()) {
            output += "     - Double sided " + lineSeperator;
        }
        else {
            output += "     - Single sided " + lineSeperator;
        }
        if(settings.isStapled()) {
            output += "     - Stapled" + lineSeperator;
        }
        else {
            output += "     - Not stapled" + lineSeperator;
        }
        return output;
    }

    private void saveFileToDirectory(Document document, Setting settings, String directory) {
        for(int i = 0; i < settings.getAmount(); i++) {
            String path =  directory + document.getName() + i + System.currentTimeMillis()+ ".txt";
            boolean flag = false;
            File printedFile = new File(path);

            try {
                flag = printedFile.createNewFile();
            } catch (IOException ioe) {
                System.out.println("Error while creating the file " + ioe);
            }

            if (flag) {
                try {
                    writeToFile(path, document, settings);
                } catch (IOException ioe) {
                    System.out.println("Error while writing to the file " + ioe);
                }
            }
        }
    }

    private void saveScan(Document document, String directory) {
        String path =  directory + document.getName() + System.currentTimeMillis()  + ".txt";
        boolean flag = false;
        File printedFile = new File(path);

        try {
            flag = printedFile.createNewFile();
        } catch (IOException ioe) {
            System.out.println("Error while creating the file " + ioe);
        }

        if (flag) {
            try {
                writeToFile(path, document);
            } catch (IOException ioe) {
                System.out.println("Error while writing to the file " + ioe);
            }
        }
    }

    private void writeToFile(String path, Document document, Setting settings) throws IOException {
        String content = getPrintedDescriptionString(settings, false) + "\n\n" + document.getText();

        writeToFile(path, document);
    }

    private void writeToFile(String path, Document document) throws IOException {
        String content = document.getText();

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.close();
    }
}
