package ReadingRoomLogin;

// --------------------------------------------------
// 관리자 전용 회원 관리 화면
// --------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class AdminFrame extends JFrame {
    private MemberManager manager;
    private DefaultListModel<Member> listModel;
    private JList<Member> memberList;

    public AdminFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("회원 관리 (관리자)");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        listModel = new DefaultListModel<>();
        memberList = new JList<>(listModel);
        memberList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getId() + " / " + value.getName() + " / " + value.getTicket());
            if (isSelected) label.setBackground(Color.LIGHT_GRAY);
            label.setOpaque(true);
            return label;
        });

        JButton refreshBtn = new JButton("새로고침");
        JButton deleteBtn = new JButton("삭제");
        JButton editBtn = new JButton("이름 수정");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(editBtn);

        add(new JScrollPane(memberList), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshList());
        deleteBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) return;
            manager.removeMember(m);
            refreshList();
        });
        editBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) return;
            String newName = JOptionPane.showInputDialog("새 이름 입력:", m.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                m.setName(newName.trim());
                manager.saveMembers();
                refreshList();
            }
        });

        refreshList();
    }

    private void refreshList() {
        listModel.clear();
        for (Member m : manager.getAllMembers()) {
            listModel.addElement(m);
        }
    }
}
