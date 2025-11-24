package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PaymentDialog extends JDialog {
    private boolean paymentSuccess = false;
    private String selectedMethod = "";
    private int amountToPay;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private int insertedAmount = 0;
    private JLabel insertedLabel;
    private JLabel messageLabel;

    private JTextField cardNumField1;
//    private JTextField cardNumField2;
//    private JTextField cardNumField3;
//    private JTextField cardNumField4;
    private JTextField expiryField;
    private JPasswordField pwField;

    public PaymentDialog(JFrame parent, String title, int price) {
        super(parent, title, true);
        this.amountToPay = price;

        setSize(400, 500);
        setLocationRelativeTo(parent);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        add(mainContainer);

        mainContainer.add(createSelectionPanel(), "결제 방식 선택");
        mainContainer.add(createCashPanel(), "현금");
        mainContainer.add(createCardPanel(), "카드");

        cardLayout.show(mainContainer, "결제 방식 선택");
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("결제 방식을 선택해주세요.", SwingConstants.CENTER);

        JLabel amountLabel = new JLabel("결제 금액: " + amountToPay + "원", SwingConstants.CENTER);
        amountLabel.setForeground(Color.BLUE);

        JButton btnCash = new JButton("현금 결제");
        JButton btnCard = new JButton("카드 결제");

        btnCash.addActionListener(e -> {
            selectedMethod = "현금";
            cardLayout.show(mainContainer, "현금");
        });

        btnCard.addActionListener(e -> {
            selectedMethod = "카드";
            cardLayout.show(mainContainer, "카드");
        });

        panel.add(label);
        panel.add(amountLabel);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        btnPanel.add(btnCash);
        btnPanel.add(btnCard);
        panel.add(btnPanel);

        return panel;
    }

    private JPanel createCashPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 여기부터 infoPanel

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(new JLabel("결제 금액: " + amountToPay + "원", SwingConstants.CENTER));

        insertedLabel = new JLabel("투입 금액: 0원", SwingConstants.CENTER);
        insertedLabel.setForeground(Color.RED);
        infoPanel.add(insertedLabel);

        messageLabel = new JLabel("지폐를 투입해주세요.", SwingConstants.CENTER);
        infoPanel.add(messageLabel);

        // 여기까지 infoPanel

        panel.add(infoPanel, BorderLayout.NORTH);

        JPanel billPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        billPanel.add(createBillButton(1000));
        billPanel.add(createBillButton(5000));
        billPanel.add(createBillButton(10000));
        billPanel.add(createBillButton(50000));

        panel.add(billPanel, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("돌아가기");
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createBillButton(int amount) {
        JButton btn = new JButton(amount + "원 투입");
        btn.addActionListener(e -> {
            insertedAmount += amount;
            insertedLabel.setText("투입 금액: " + insertedAmount + "원");

            if (insertedAmount >= amountToPay) {
                int change = insertedAmount - amountToPay;
                String msg = "결제 성공!";
                if (change > 0) {
                    msg += "\n거스름돈 " + change + "원을 반환합니다.";
                }
                JOptionPane.showMessageDialog(this, msg);
                paymentSuccess = true;
                dispose();
            }
        });
        return btn;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("카드 정보를 입력하세요."));

        cardNumField1 = new JTextField();
        cardNumField1.setBorder(BorderFactory.createTitledBorder("카드번호 (16자리)"));
        panel.add(cardNumField1);

        expiryField = new JTextField();
        expiryField.setBorder(BorderFactory.createTitledBorder("유효기간 (4자리 MMYY)"));
        panel.add(expiryField);

        pwField = new JPasswordField();
        pwField.setBorder(BorderFactory.createTitledBorder("비밀번호 앞 2자리"));
        panel.add(pwField);

        JButton payBtn = new JButton("결제하기");
        JButton cancelBtn = new JButton("돌아가기");

        payBtn.addActionListener(this::handleCardPayment);
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        btnPanel.add(payBtn);
        btnPanel.add(cancelBtn);

        panel.add(btnPanel);

        return panel;
    }

    private void handleCardPayment(ActionEvent e) {
        String cardNum = cardNumField1.getText().trim();
        String expiry = expiryField.getText().trim();
        String pw = new String(pwField.getPassword()).trim();

        if (cardNum.length() != 16 || !cardNum.matches("[0-9]+")) {
            JOptionPane.showMessageDialog(this, "카드번호 16자리를 정확히 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (expiry.length() != 4 || !expiry.matches("[0-9]+")) {
            JOptionPane.showMessageDialog(this, "유효기간 4자리를 정확히 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pw.length() != 2 || !pw.matches("[0-9]+")) {
            JOptionPane.showMessageDialog(this, "비밀번호 앞 2자리를 정확히 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "카드 승인 완료!");
        paymentSuccess = true;
        dispose();
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }
}