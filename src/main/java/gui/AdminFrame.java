package gui;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import javax.swing.*;
import java.awt.*;

class AdminFrame extends JFrame {
    private MemberManager manager;
    private DefaultListModel<Member> listModel;
    private JList<Member> memberList;

    public AdminFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("회원 관리 (관리자 모드)");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container cp = getContentPane();
        cp.setBackground(Theme.BACKGROUND_COLOR);
        cp.setLayout(new BorderLayout());

        // 1.제목
        JLabel titleLabel = new JLabel("회원 관리 시스템", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT); // 폰트 통일
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        cp.add(titleLabel, BorderLayout.NORTH);

        // 2.중앙
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));

        JPanel headerPanel = new JPanel(new GridLayout(1, 3));
        headerPanel.setBackground(new Color(220, 230, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        Font headerFont = new Font("Malgun Gothic", Font.BOLD, 16);

        JLabel h1 = new JLabel("아이디", SwingConstants.CENTER); h1.setFont(headerFont);
        JLabel h2 = new JLabel("이름", SwingConstants.CENTER);   h2.setFont(headerFont);
        JLabel h3 = new JLabel("이용권 정보", SwingConstants.CENTER); h3.setFont(headerFont);
        headerPanel.add(h1);
        headerPanel.add(h2);
        headerPanel.add(h3);
        centerPanel.add(headerPanel, BorderLayout.NORTH);
        listModel = new DefaultListModel<>();
        memberList = new JList<>(listModel);
        memberList.setFixedCellHeight(50);
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        memberList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel panel = new JPanel(new GridLayout(1, 3));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            //수정하기
            String ticketInfo = "미보유";
            if (value.getTicket() != null) {
                ticketInfo = value.getTicket().getClass().getSimpleName().replace("Ticket", "") + " 이용권";
            }
            JLabel l1 = new JLabel(value.getId(), SwingConstants.CENTER);
            JLabel l2 = new JLabel(value.getName(), SwingConstants.CENTER);
            JLabel l3 = new JLabel(ticketInfo, SwingConstants.CENTER);

            l1.setFont(Theme.MAIN_FONT);
            l2.setFont(Theme.MAIN_FONT);
            l3.setFont(Theme.MAIN_FONT);

            if (isSelected) {
                panel.setBackground(Theme.PRIMARY_COLOR);
                l1.setForeground(Color.WHITE);
                l2.setForeground(Color.WHITE);
                l3.setForeground(Color.WHITE);
            } else {
                panel.setBackground(Color.WHITE);
                l1.setForeground(Theme.TEXT_COLOR);
                l2.setForeground(Theme.TEXT_COLOR);
                l3.setForeground(Theme.TEXT_COLOR);
            }

            panel.add(l1);
            panel.add(l2);
            panel.add(l3);

            return panel;
        });

        JScrollPane scrollPane = new JScrollPane(memberList);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(SystemColor.controlHighlight));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        cp.add(centerPanel, BorderLayout.CENTER);

        // 3. 아래
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton refreshBtn = new JButton("새로고침");
        JButton editBtn = new JButton("이름 수정");
        JButton deleteBtn = new JButton("회원 삭제");

        Theme.styleButton(refreshBtn);
        Theme.styleButton(editBtn);
        Theme.styleButton(deleteBtn);
        deleteBtn.setBackground(Theme.OCCUPIED_SEAT);

        btnPanel.add(refreshBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        cp.add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshList());

        deleteBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) {
                JOptionPane.showMessageDialog(this, "삭제할 회원을 선택하세요.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, m.getName() + " 회원을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.removeMember(m);
                refreshList();
            }
        });

        editBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) {
                JOptionPane.showMessageDialog(this, "수정할 회원을 선택하세요.");
                return;
            }
            String newName = JOptionPane.showInputDialog(this, "새 이름 입력:", m.getName());
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