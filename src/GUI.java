import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GUI extends JFrame{

    private Printer printer;

    private JButton print;
    private JButton scan;
    private JButton fillInk;
    private JButton fillPaper;
    private JButton fillStaples;

    private JCheckBox isDoubleSided;
    private JCheckBox isColoured;
    private JCheckBox isStapled;
    private JTextField amount;
    private JComboBox<String> size;

    private JTextArea output;

    private JFileChooser chooser;

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.setVisible(true);
    }

    public GUI() {
        super();
        setSize(600, 450);
        setLayout(new BorderLayout());
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        printer = new Printer();
        chooser = new JFileChooser();

        //add Buttons
        addButtons();

        //add print and scan options
        addPrintAndScanOptions();

        //add Output
        addOutput();
    }

    private void addButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        print = new JButton("Print");
        print.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // call function that should be executed when print is pressed
                print();
            }
        });

        scan = new JButton("Scan");
        scan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // call function that should be executed when scan is pressed
                scan();
            }
        });
        fillInk = new JButton("Fill Ink");
        fillInk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillInk();
            }
        });

        fillPaper = new JButton("Fill paper");
        fillPaper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillPaper();
            }
        });

        fillStaples = new JButton("Fill staples");
        fillStaples.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillStaples();
            }
        });

        buttonPanel.add(print);
        buttonPanel.add(scan);
        buttonPanel.add(fillInk);
        buttonPanel.add(fillPaper);
        buttonPanel.add(fillStaples);

        this.add(buttonPanel, BorderLayout.NORTH);
    }

    private void addPrintAndScanOptions() {
        String[] sizeOptions = {"A4", "A3"};
        isDoubleSided = new JCheckBox();
        isColoured = new JCheckBox();
        isStapled = new JCheckBox();
        amount = new JTextField();
        amount.setPreferredSize(new Dimension(40,20));
        size = new JComboBox<>(sizeOptions);


        JLabel amountLabel = new JLabel("Amount: ");
        JLabel sizeLabel = new JLabel("Size: ");
        JLabel doubleSided = new JLabel("Double sided:");
        JLabel coloured = new JLabel("Coloured:");
        JLabel stapled = new JLabel("Is Stapled:");

        JPanel printAndScanPanel = new JPanel();
        printAndScanPanel.setLayout(new BorderLayout());
        JPanel printPanel = new JPanel();
        printPanel.setLayout(new GridLayout(5,1));
        JPanel scanPanel = new JPanel();
        scanPanel.setLayout(new FlowLayout());

        JPanel[] printRows = new JPanel[5];
        for(int i = 0; i < 5; i ++ ) {
            printRows[i] = new JPanel();
            printRows[i].setLayout(new FlowLayout());
        }
        printRows[0].add(amountLabel);
        printRows[0].add(amount);

        printRows[1].add(sizeLabel);
        printRows[1].add(size);

        printRows[2].add(doubleSided);
        printRows[2].add(isDoubleSided);

        printRows[3].add(coloured);
        printRows[3].add(isColoured);

        printRows[4].add(stapled);
        printRows[4].add(isStapled);

        for(int i = 0; i < 5; i ++ ) {
            printPanel.add(printRows[i]);
        }

        printAndScanPanel.add(printPanel, BorderLayout.WEST);
        printAndScanPanel.add(scanPanel, BorderLayout.EAST);
        this.add(printAndScanPanel, BorderLayout.CENTER);
    }

    private void addOutput() {
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new FlowLayout());

        output = new JTextArea();
        output.setPreferredSize(new Dimension(500, 200));
        output.setText("Output");

        JScrollPane scroll = new JScrollPane(output);

        outputPanel.add(scroll);
        add(outputPanel, BorderLayout.SOUTH);
    }

    private void print() {
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String filename = file.getName();
                int pos = filename.indexOf('.');
                String documentName = filename.substring(0, pos);

                int amountPages = getAmountPages(filename);


                Document document = new Document(amountPages, readFile("toPrint/" + filename), documentName);

                Setting setting = createSettings();
                output.setText(printer.print(document, setting));
            }
            catch (Exception e) {
                System.out.println("Problem accessing file");
            }
        }
        else {
            //File choosing cancelled
            System.out.println("File choosing cancelled");
        }
    }

    private int getAmountPages(String filePath) {
        Path path = Paths.get("toPrint/" + filePath);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String currentLine = null;
            int lines = 0;
            while ((currentLine = reader.readLine()) != null) {
                lines++;
            }
            int pages = 0;
            while(lines > 0) {
                pages++;
                lines = lines - 30;
            }
            return pages;
        }
        catch(Exception e) {
            System.out.println("problem");
            return 1;
        }

    }

    private Setting createSettings() {
        int amountCopy;
        String paperSize;
        boolean doubleSided;
        boolean coloured;
        boolean stapled;

        try {
            amountCopy = Integer.parseInt(amount.getText());
            paperSize = (String) size.getSelectedItem();
            doubleSided = isDoubleSided.isSelected();
            coloured = isColoured.isSelected();
            stapled = isStapled.isSelected();

            Setting setting = new Setting(amountCopy, coloured, doubleSided, stapled, paperSize);
            return setting;
        }
        catch (Exception e) {
            System.out.println("Something went wrong." + e);
        }
        return new Setting();
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    private void scan() {
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String filePath = file.getPath();

                output.setText(printer.scan(filePath));

                String name = file.getName();

                sendToEmail(name);
            }
            catch (Exception e) {
                System.out.println("Problem accessing file");
            }
        }
        else {
            //File choosing cancelled
            System.out.println("File choosing cancelled");
        }
    }

    private void sendToEmail(String fileName) {
        JLabel question = new JLabel("Would you like to send it via email?");
        int result = JOptionPane.showConfirmDialog(this, question, "Sending via email", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(result == 0) {
            //Person wants to send it via email
            String emailString = getEmails();
            String[] emails = emailString.split(";");
            if(emails.length > 1) {
                boolean passTrue = checkPassword();
                boolean cancelSending;

                while(!passTrue) {
                    cancelSending = cancelSending();
                    if(cancelSending) {
                        break;
                    }
                    else {
                        passTrue = checkPassword();
                    }
                }
                if(passTrue) {
                    for (String mail:emails) {
                        printer.sendScanToMail(fileName, mail);
                    }
                }
            }
            else {
                printer.sendScanToMail(fileName, emails[0]);
            }
        }

    }

    private String getEmails() {
        String[] options = {"OK"};
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        JLabel label = new JLabel("Please enter the email(s). Seperate multiple emails by ';'.");
        JTextArea txt = new JTextArea();
        txt.setPreferredSize(new Dimension(400, 200));
        panel.add(label);
        panel.add(txt);

        int result = JOptionPane.showOptionDialog(null, panel, "Enter email(s)", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if(result == 0) {
            String emails = txt.getText();
            if(emails.trim().equals("")) {
                return getEmails();
            }
            else {
                return emails;
            }
        }
        return "-1";
    }

    private boolean checkPassword() {
        String[] options = {"OK"};
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        JLabel label = new JLabel("Please enter the password");
        JPasswordField txt = new JPasswordField();
        txt.setPreferredSize(new Dimension(200, 20));
        panel.add(label);
        panel.add(txt);

        int result = JOptionPane.showOptionDialog(null, panel, "Enter password", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if(result == 0) {
            char[] passChars = txt.getPassword();
            String pass = "";
            for (char character:passChars) {
                pass += character;
            }

            if(pass.equals("1234")) {
                return true;
            }
            else {
                return false;
            }

        }
        return false;
    }

    private boolean cancelSending() {
        int result = JOptionPane.showConfirmDialog(null, "Wrong password. \nWould you like to cancel sending the mail?", "Cancel", JOptionPane.YES_NO_OPTION);
        if(result == JOptionPane.YES_OPTION) {
            return true;
        }
        else {
            return false;
        }
    }

    private void fillInk() {
        output.setText(printer.fillInk());
    }

    private void fillPaper() {
        output.setText(printer.fillPaper());
    }

    private void fillStaples() {
        output.setText(printer.fillStaples());
    }
}
