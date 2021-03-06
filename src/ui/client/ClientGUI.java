package ui.client;

import main.MainPubSub;
import ui.IGUI;
import client.IClient;
import server.Article;
import server.IServer;
import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by regmoraes on 19/04/15.
 */
public class ClientGUI extends JFrame implements IGUI,IClientGUI{

    private JList listArticles;
    private JPanel rootPanel;
    private JButton buttonSubscribe;
    private JTextField textFieldArticleCategory;
    private JTextArea textAreaArticleContent;
    private JTextField textFieldArticleTitle;
    private JButton buttonPublish;
    private JLabel labelCurrentSubscriptions;
    private JList listSubscriptions;
    private JLabel labelCategory;
    private JComboBox comboBoxCategories;

    public MainPubSub mainPubSub = MainPubSub.getInstance();
    public static ClientGUI instance;

    List<Article> myArticles = new ArrayList<>();
    List<String> mySubscriptions = new ArrayList<>();
    DefaultListModel listModelSubscriptions;
    DefaultListModel listModelArticles;

    public static ClientGUI getInstance(){

        if(instance == null){
            return new ClientGUI();

        }else {
            return instance;
        }
    }

    @Override
    public void initializeGUI(){

        setContentPane(rootPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeListeners();
        setVisible(true);
    }

    public void initializeListeners(){

        buttonPublish.addActionListener(actionListener -> {

            if (!textFieldArticleCategory.getText().equals("") &&
                    !textFieldArticleCategory.getText().equals("") &&
                    !textAreaArticleContent.getText().equals("")) {

                try {

                    IServer server = mainPubSub.server;

                    Article a = new Article(textFieldArticleCategory.getText(), textFieldArticleTitle.getText(),
                            textAreaArticleContent.getText());

                    server.publish(a);
                    updateSubscriptionsCategory();

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            } else {

                //mainPubSub.showErrorMessage("Article fields must be filled");
            }
        });

        buttonSubscribe.addActionListener(actionListener -> {

            try {

                IServer server = mainPubSub.server;
                IClient client = mainPubSub.client;

                server.subscribe(client, (String) comboBoxCategories.getSelectedItem());
                updateMySubscriptions((String) comboBoxCategories.getSelectedItem());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateMySubscriptions(String keyword) throws RemoteException {

        mySubscriptions.add(keyword);
        listModelSubscriptions = new DefaultListModel<String>();
        mySubscriptions.forEach(listModelSubscriptions::addElement);
        listSubscriptions.setModel(listModelSubscriptions);
    }

    private void updateSubscriptionsCategory() throws NotBoundException,RemoteException {

        IServer server = mainPubSub.server;

        List<String> list = server.getSubscriptionsCategory();

        if(list != null){

            DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel<String>();

            list.forEach(comboBoxModel::addElement);

            comboBoxCategories.setModel(comboBoxModel);
        }
    }

    @Override
    public void showNewArticles(Article a) {

        myArticles.add(a);
        listModelArticles = new DefaultListModel<String>();

        for(Article article : myArticles){

            String rowInfo = "["+article.getKeyword().toUpperCase()+"] "+article.getTitle();

            listModelArticles.addElement(rowInfo);
        }

        listArticles.setModel(listModelArticles);
        System.out.println("Article received");
    }
}
