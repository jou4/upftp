(defproject upftp "0.0.1"
  :author "K. Kamitsukasa"
  :description "FTP Client with Clojure"
  :main upftp
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [commons-net/commons-net "2.0"
                  :exclusions [org.apache.commons.net.bsd
                               org.apache.commons.net.chargen
                               org.apache.commons.net.daytime
                               org.apache.commons.net.discard
                               org.apache.commons.net.echo
                               org.apache.commons.net.finger
                               org.apache.commons.net.io
                               org.apache.commons.net.nntp
                               org.apache.commons.net.ntp
                               org.apache.commons.net.pop3
                               org.apache.commons.net.smtp
                               org.apache.commons.net.telnet
                               org.apache.commons.net.tftp
                               org.apache.commons.net.time
                               org.apache.commons.net.util
                               org.apache.commons.net.whois]]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [vimclojure/server "2.2.0"]
                     [org.clojars.autre/lein-vimclojure "1.0.0"]]
  :repositories {"clojars" "http://clojars.org/repo"
                 "apache-releases" "https://repository.apache.org/content/repositories/releases/"}
  :uberjar-name "upftp.jar")
