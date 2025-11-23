package gui;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout; // FlowLayout import 추가
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField; // JTextField import 추가
import javax.swing.event.DocumentListener; // DocumentListener import 추가
import javax.swing.event.DocumentEvent; // DocumentEvent import 추가

class AdminFrame extends JFrame {
    private MemberManager manager;
    private DefaultListModel<Member> listModel;
    private JList<Member> memberList;
    private JTextField searchField; // 🌟 1. 검색 필드 추가

    public AdminFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("회원 관리 (관리자)");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        listModel = new DefaultListModel<>();
        memberList = new JList<>(listModel);
        memberList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            // Member 객체에 getTicket()이 없거나 null일 경우 대비
            String ticketInfo = (value.getTicket() != null) ? value.getTicket().toString() : "N/A";
            JLabel label = new JLabel(value.getId() + " / " + value.getName() + " / " + ticketInfo);
            
            if (isSelected) label.setBackground(Color.LIGHT_GRAY);
            label.setOpaque(true);
            return label;
        });

        // 🌟 2. 검색 패널 생성 (NORTH에 배치)
        searchField = new JTextField(20);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("ID 또는 이름 검색:"));
        searchPanel.add(searchField);
        
        // 🌟 3. 검색 필드 리스너 추가 (입력 즉시 검색)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
            public void removeUpdate(DocumentEvent e) {
                filterList();
            }
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }
        });


        JButton refreshBtn = new JButton("새로고침");
        JButton deleteBtn = new JButton("삭제");
        JButton editBtn = new JButton("이름 수정");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(editBtn);

        // 🌟 4. 검색 패널을 NORTH에 배치
        add(searchPanel, BorderLayout.NORTH); 
        add(new JScrollPane(memberList), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshList());
        deleteBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) return;
            // 삭제 확인 대화상자 추가 (선택 사항)
            int confirm = JOptionPane.showConfirmDialog(this, 
                "회원 [" + m.getName() + "]을(를) 정말 삭제하시겠습니까?", 
                "회원 삭제 확인", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                manager.removeMember(m);
                // 삭제 후 검색 상태 유지하며 목록 갱신
                filterList(); 
            }
        });
        editBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) return;
            String newName = JOptionPane.showInputDialog("새 이름 입력:", m.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                m.setName(newName.trim());
                manager.saveMembers();
                // 수정 후 검색 상태 유지하며 목록 갱신
                filterList(); 
            }
        });

        refreshList();
    }

    // 🌟 5. 전체 목록을 로드하는 기존 메서드
    private void refreshList() {
        // 검색 필드가 비어 있으면 전체 목록을 보여줍니다.
        if (searchField.getText().trim().isEmpty()) {
            listModel.clear();
            for (Member m : manager.getAllMembers()) {
                listModel.addElement(m);
            }
        } else {
            // 검색어가 있으면 필터링된 목록을 보여줍니다.
            filterList();
        }
    }
    
    // 🌟 6. 검색 로직을 담당하는 새로운 필터 메서드
    private void filterList() {
        String searchText = searchField.getText().trim().toLowerCase();
        listModel.clear();
        
        if (searchText.isEmpty()) {
            // 검색어가 없으면 전체 목록을 다시 로드
            for (Member m : manager.getAllMembers()) {
                listModel.addElement(m);
            }
            return;
        }

        for (Member m : manager.getAllMembers()) {
            // ID 또는 이름에 검색어가 포함되어 있는지 확인 (부분 검색)
            boolean idMatch = m.getId().toLowerCase().contains(searchText);
            boolean nameMatch = m.getName().toLowerCase().contains(searchText);
            
            if (idMatch || nameMatch) {
                listModel.addElement(m);
            }
        }
    }
}
