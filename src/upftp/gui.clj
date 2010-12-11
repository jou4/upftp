(ns upftp.gui
  (:import [javax.swing JOptionPane UIManager JFrame JPanel
            JLabel JButton JFileChooser BoxLayout
            JMenuBar JMenu JMenuItem]
           [javax.swing.filechooser FileNameExtensionFilter]
           [java.io File]
           [java.awt.event ActionListener]))

(try
 (UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))
 (catch Exception e (.printStackTrace e)))

(defn action-listener [callback]
  (proxy [ActionListener] []
    (actionPerformed [e] (callback))))

(defn show-message
  ([msg]
     (show-message msg nil))
  ([msg parent]
     (JOptionPane/showMessageDialog parent msg)))

(defn file-dialog [action parent callback filter current-dir]
  (let [chooser (JFileChooser.)]
    (.setFileSelectionMode chooser JFileChooser/FILES_ONLY)
    (when-not (nil? filter) (.setFileFilter chooser filter))
    (if (nil? current-dir)
      (.setCurrentDirectory chooser (.. (File. ".") getAbsoluteFile getParentFile))
      (.setCurrentDirectory chooser current-dir))
    (when (= JFileChooser/APPROVE_OPTION
             (if (= :save action)
               (.showSaveDialog chooser parent)
               (.showOpenDialog chooser parent)))
      (callback (.. chooser getSelectedFile getPath)))))

(defn open-dialog [frame callback]
  (file-dialog :open frame callback
                (FileNameExtensionFilter. "タスクファイル(.clj)" (into-array ["clj"]))
                nil))

(defn select-file [callback]
  (let [frame (JFrame. "FTUP - タスクファイル選択")
        panel (JPanel.)
        open-action (action-listener #(open-dialog frame callback))]
    (doto panel
      (.setLayout (BoxLayout. panel BoxLayout/Y_AXIS))
      (.add (let [p (JPanel.)]
              (.add p (JLabel. "タスクファイルを選択してください。"))
              p))
      (.add (let [p (JPanel.)
                  b (JButton. "ファイル選択")]
              (.addActionListener b open-action)
              (.add p b)
              p)))
    (doto frame
      (.add panel)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setSize 300 100)
      (.setResizable false)
      (.setLocationRelativeTo nil)
      (.setVisible true))
    frame))

(defn main [callbacks]
  (let [frame (JFrame. "FTUP")
        panel (JPanel.)
        menubar (JMenuBar.)
        menu-file (JMenu. "ファイル")
        open-action (action-listener #(open-dialog frame (callbacks :open)))
        execute-action (action-listener (callbacks :execute))
        context (atom {:frame frame})]
    (doto menu-file
      (.add (let [item (JMenuItem. "開く")]
              (.addActionListener item open-action)
              item))
      (.add (let [item (JMenuItem. "終了")]
              (.addActionListener item (action-listener #(System/exit 0)))
              item)))
    (doto menubar
      (.add menu-file))
    (doto panel
      (.add (let [p (JPanel.)
                  b (JButton. "実行")]
              (reset! context (assoc @context :set-enabled #(.setEnabled b %)))
              (.addActionListener b execute-action)
              (.setEnabled b false)
              (.add p b)
              p)))
    (doto frame
      (.setJMenuBar menubar)
      (.add panel)
     ; (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setSize 300 100)
      (.setResizable false)
      (.setLocationRelativeTo nil)
      (.setVisible true))
    @context))
