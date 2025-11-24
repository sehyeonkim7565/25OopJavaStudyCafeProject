package gui; 

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class AdminFrame extends JFrame {
    private MemberManager manager;
    private DefaultListModel<Member> listModel;
    private JList<Member> memberList;
    private DefaultListModel<UsageEntry> usageListModel;
    private JList<UsageEntry> usageList;
    private JTextField searchField;
    private Timer logRefreshTimer;
    private static final String USAGE_LOG_PATH = "logs/usage.jsonl";
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("회원 관리 (관리자 모드)");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container cp = getContentPane();
        cp.setBackground(Theme.BACKGROUND_COLOR);
        cp.setLayout(new BorderLayout());

        // 상단 타이틀
        JLabel titleLabel = new JLabel("회원 관리 시스템", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        cp.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        memberList = new JList<>(listModel);
        memberList.setFixedCellHeight(50);
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel panel = new JPanel(new GridLayout(1, 3));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            String ticketInfo = (value.getTicket() != null)
                    ? value.getTicket().getClass().getSimpleName().replace("Ticket", "") + " 이용권"
                    : "미보유";
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

        // 검색 패널
        searchField = new JTextField(20);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        searchPanel.add(new JLabel("ID 또는 이름 검색:"));
        searchPanel.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterList(); }
            public void removeUpdate(DocumentEvent e) { filterList(); }
            public void insertUpdate(DocumentEvent e) { filterList(); }
        });

        // 리스트 헤더
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

        JScrollPane memberScroll = new JScrollPane(memberList);
        memberScroll.getViewport().setBackground(Color.WHITE);
        memberScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235)));

        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.setBackground(Theme.BACKGROUND_COLOR);
        memberPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        memberPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(Theme.BACKGROUND_COLOR);
        listWrapper.add(headerPanel, BorderLayout.NORTH);
        listWrapper.add(memberScroll, BorderLayout.CENTER);

        memberPanel.add(listWrapper, BorderLayout.CENTER);

        // 우측: 입퇴실 로그 영역
        usageListModel = new DefaultListModel<>();
        usageList = new JList<>(usageListModel);
        usageList.setFixedCellHeight(40);
        usageList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            String end = value.endTime == null ? "-" : value.endTime;
            long duration = (value.type != null && value.type.equals("MOVE"))
                    ? -1
                    : (value.endTime == null ? computeDuration(value) : value.durationMinutes);

            Font timeFont = new Font("Monospaced", Font.PLAIN, 12);

            JLabel c1 = new JLabel(nvl(value.memberId), SwingConstants.CENTER);
            JLabel c2 = new JLabel(nvl(value.seatNumber), SwingConstants.CENTER);
            JLabel c3 = new JLabel(nvl(value.startTime), SwingConstants.CENTER);
            JLabel c4 = new JLabel(end, SwingConstants.CENTER);
            JLabel c5 = new JLabel(duration < 0 ? "-" : String.valueOf(duration), SwingConstants.CENTER);

            c1.setFont(Theme.MAIN_FONT);
            c2.setFont(Theme.MAIN_FONT);
            c3.setFont(timeFont);
            c4.setFont(timeFont);
            c5.setFont(Theme.MAIN_FONT);

            if (isSelected) {
                row.setBackground(Theme.PRIMARY_COLOR);
                c1.setForeground(Color.WHITE);
                c2.setForeground(Color.WHITE);
                c3.setForeground(Color.WHITE);
                c4.setForeground(Color.WHITE);
                c5.setForeground(Color.WHITE);
            } else {
                row.setBackground(Color.WHITE);
                c1.setForeground(Theme.TEXT_COLOR);
                c2.setForeground(Theme.TEXT_COLOR);
                c3.setForeground(Theme.TEXT_COLOR);
                c4.setForeground(Theme.TEXT_COLOR);
                c5.setForeground(Theme.TEXT_COLOR);
            }

            row.add(c1);
            row.add(c2);
            row.add(c3);
            row.add(c4);
            row.add(c5);
            return row;
        });

        JPanel usageHeader = new JPanel(new GridLayout(1, 5, 10, 0));
        usageHeader.setBackground(new Color(220, 230, 240));
        usageHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        Font uFont = new Font("Malgun Gothic", Font.BOLD, 14);
        JLabel uh1 = new JLabel("ID", SwingConstants.CENTER); uh1.setFont(uFont);
        JLabel uh2 = new JLabel("좌석", SwingConstants.CENTER); uh2.setFont(uFont);
        JLabel uh3 = new JLabel("입실시각", SwingConstants.CENTER); uh3.setFont(uFont);
        JLabel uh4 = new JLabel("퇴실시각", SwingConstants.CENTER); uh4.setFont(uFont);
        JLabel uh5 = new JLabel("분", SwingConstants.CENTER); uh5.setFont(uFont);
        usageHeader.add(uh1); usageHeader.add(uh2); usageHeader.add(uh3); usageHeader.add(uh4); usageHeader.add(uh5);

        JScrollPane usageScroll = new JScrollPane(usageList);
        usageScroll.getViewport().setBackground(Color.WHITE);
        usageScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235)));

        JPanel usagePanel = new JPanel(new BorderLayout());
        usagePanel.setBackground(Theme.BACKGROUND_COLOR);
        usagePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        usagePanel.add(usageHeader, BorderLayout.NORTH);
        usagePanel.add(usageScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, memberPanel, usagePanel);
        splitPane.setResizeWeight(0.5);

        cp.add(splitPane, BorderLayout.CENTER);

        JButton editBtn = new JButton("이름 수정");
        JButton deleteBtn = new JButton("회원 삭제");
        JButton refreshBtn = new JButton("새로고침");

        Theme.styleButton(editBtn);
        Theme.styleButton(deleteBtn);
        Theme.styleButton(refreshBtn);
        Font btnFont = Theme.MAIN_FONT.deriveFont(13f);
        editBtn.setFont(btnFont);
        deleteBtn.setFont(btnFont);
        refreshBtn.setFont(btnFont);
        editBtn.setPreferredSize(new Dimension(140, 36));
        deleteBtn.setPreferredSize(new Dimension(140, 36));
        refreshBtn.setPreferredSize(new Dimension(140, 36));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(Theme.BACKGROUND_COLOR);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        cp.add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            refreshList();
            refreshUsageLog();
        });
        deleteBtn.addActionListener(e -> {
            Member m = memberList.getSelectedValue();
            if (m == null) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "회원 [" + m.getName() + "]을(를) 삭제하시겠습니까?",
                    "회원 삭제 확인",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.removeMember(m);
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
                filterList();
            }
        });

        refreshList();
        refreshUsageLog();

        // 1분마다 로그 새로고침 (진행 중 세션의 경과 시간 반영)
        logRefreshTimer = new Timer(60_000, e -> refreshUsageLog());
        logRefreshTimer.start();
    }

    private void refreshList() {
        if (searchField.getText().trim().isEmpty()) {
            listModel.clear();
            for (Member m : manager.getAllMembers()) {
                listModel.addElement(m);
            }
        } else {
            filterList();
        }
    }

    private void refreshUsageLog() {
        try {
            if (!Files.exists(Paths.get(USAGE_LOG_PATH))) {
                usageListModel.clear();
                return;
            }
            List<String> lines = Files.readAllLines(Paths.get(USAGE_LOG_PATH), StandardCharsets.UTF_8);
            List<UsageEntry> entries = parseUsageEntries(lines);
            usageListModel.clear();
            for (UsageEntry e : entries) {
                usageListModel.addElement(e);
            }
        } catch (Exception ex) {
            usageListModel.clear();
            usageListModel.addElement(new UsageEntry("오류", "-", "-", ex.getMessage(), 0));
        }
    }

    private void filterList() {
        String searchText = searchField.getText().trim().toLowerCase();
        listModel.clear();
        if (searchText.isEmpty()) {
            for (Member m : manager.getAllMembers()) {
                listModel.addElement(m);
            }
            return;
        }

        for (Member m : manager.getAllMembers()) {
            boolean idMatch = m.getId().toLowerCase().contains(searchText);
            boolean nameMatch = m.getName().toLowerCase().contains(searchText);
            if (idMatch || nameMatch) {
                listModel.addElement(m);
            }
        }
    }

    private List<UsageEntry> parseUsageEntries(List<String> lines) {
        List<UsageEntry> entries = new ArrayList<>();
        Gson gson = new Gson();
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            try {
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(line).getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "SESSION";
                if ("MOVE".equals(type)) {
                    UsageEntry move = new UsageEntry();
                    move.type = "MOVE";
                    move.memberId = obj.has("memberId") ? obj.get("memberId").getAsString() : "-";
                    move.fromSeat = obj.has("fromSeat") ? obj.get("fromSeat").getAsString() : "-";
                    move.toSeat = obj.has("toSeat") ? obj.get("toSeat").getAsString() : "-";
                    move.seatNumber = move.fromSeat + "→" + move.toSeat;
                    move.startTime = obj.has("movedAt") ? obj.get("movedAt").getAsString() : "-";
                    move.endTime = "-";
                    move.durationMinutes = 0;
                    entries.add(move);
                } else {
                    UsageEntry entry = gson.fromJson(line, UsageEntry.class);
                    entry.type = "SESSION";
                    entries.add(entry);
                }
            } catch (JsonSyntaxException ignore) {
            }
        }
        return entries;
    }

    private long computeDuration(UsageEntry e) {
        if (e.endTime != null) return e.durationMinutes;
        if (e.startTime == null) return e.durationMinutes;
        try {
            LocalDateTime start = LocalDateTime.parse(e.startTime, LOG_TIME_FORMAT);
            return Math.max(0, java.time.Duration.between(start, LocalDateTime.now()).toMinutes());
        } catch (Exception ex) {
            return e.durationMinutes;
        }
    }

    private String nvl(String s) {
        return (s == null) ? "-" : s;
    }

    private static class UsageEntry {
        String type;          // SESSION or MOVE
        String memberId;
        String seatNumber;    // MOVE의 경우 "A→B" 형태로 변환
        String fromSeat;
        String toSeat;
        String startTime;     // 입실시각 / 이동시각
        String endTime;       // 퇴실시각
        long durationMinutes;

        UsageEntry() {}
        UsageEntry(String memberId, String seatNumber, String startTime, String endTime, long durationMinutes) {
            this.type = "SESSION";
            this.memberId = memberId;
            this.seatNumber = seatNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
        }
    }
}
